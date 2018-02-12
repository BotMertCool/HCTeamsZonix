package us.zonix.hcfactions.util;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.WorldSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.zonix.core.CorePlugin;
import us.zonix.core.tab.TabList;
import us.zonix.hcfactions.FactionsPlugin;
import us.zonix.hcfactions.event.Event;
import us.zonix.hcfactions.event.EventManager;
import us.zonix.hcfactions.event.koth.KothEvent;
import us.zonix.hcfactions.factions.Faction;
import us.zonix.hcfactions.factions.claims.Claim;
import us.zonix.hcfactions.factions.type.PlayerFaction;
import us.zonix.hcfactions.factions.type.SystemFaction;
import us.zonix.hcfactions.profile.Profile;
import us.zonix.hcfactions.profile.cooldown.ProfileCooldownType;
import us.zonix.hcfactions.profile.options.item.ProfileOptionsItemState;
import us.zonix.hcfactions.util.player.PlayerUtility;

import java.util.*;

public class TabListRunnable extends BukkitRunnable {

    TabList tabList = CorePlugin.getInstance().getTabListManager().getTabList();

    @Override
    public void run() {

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {

            Profile profile = Profile.getByPlayer(player);

            if (profile == null) {
                continue;
            }

            if (profile.getOptions().getModifyTabList() == ProfileOptionsItemState.TAB_VANILLA) {

                int row = 0;
                int column = 0;

                for(Player online : PlayerUtility.getOnlinePlayers()) {


                    if(row == 20) {
                        row = 0;
                        column++;
                    }

                    tabList.updateSlot(player, row, column, this.getRelationWithPlayer(player, online) + online.getName());
                    row++;
                }

                continue;
            }


            tabList.updateSlot(player, 0, 1, ChatColor.DARK_RED.toString() + ChatColor.BOLD +  "ZONIX " + ChatColor.GRAY + "(HCF)");

            tabList.updateSlot(player, 1, 1, ChatColor.RED + "Players Online");
            tabList.updateSlot(player, 2, 1, ChatColor.GRAY.toString() + PlayerUtility.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers());

            tabList.updateSlot(player, 0, 0, ChatColor.RED + "Player Info");
            tabList.updateSlot(player, 1, 0, ChatColor.GRAY + "Kills: " + profile.getKillCount());
            tabList.updateSlot(player, 2, 0, ChatColor.GRAY + "Deaths: " + profile.getDeathCount());
            tabList.updateSlot(player, 3, 0, ChatColor.GRAY + "Balance: " + "$" + profile.getBalance());

            tabList.updateSlot(player, 5, 0, ChatColor.RED + "Your Location");

            Claim claim = Claim.getProminentClaimAt(player.getLocation());

            if (claim != null) {
                tabList.updateSlot(player, 6, 0, this.getFixedFactionName(player, claim.getFaction()));
            } else {
                tabList.updateSlot(player, 6, 0, ChatColor.DARK_GREEN + "Wilderness");
            }

            final String direction = this.getCardinalDirection(player);
            if (direction != null) {
                tabList.updateSlot(player, 7, 0, ChatColor.GRAY + "(" + player.getLocation().getBlockX() + ", " + player.getLocation().getBlockZ() + ") [" + direction + "]");
            } else {
                tabList.updateSlot(player, 7, 0, ChatColor.GRAY + "(" + player.getLocation().getBlockX() + ", " + player.getLocation().getBlockZ() + ")");
            }

            tabList.updateSlot(player, 9, 0, ChatColor.RED + "Faction Info");


            PlayerFaction faction = Profile.getByPlayer(player).getFaction();

            if (faction != null) {
                tabList.updateSlot(player, 10, 0, ChatColor.GRAY + "DTR: " + faction.getDeathsTillRaidable());
                tabList.updateSlot(player, 11, 0, ChatColor.GRAY + "Online: " + faction.getOnlinePlayers().size() + "/" + faction.getMembers().size());
                tabList.updateSlot(player, 12, 0, ChatColor.GRAY + "Balance: " + "$" + faction.getBalance());
                if (faction.getHome() != null) {
                    Location homeLocation = LocationSerialization.deserializeLocation(faction.getHome());
                    tabList.updateSlot(player, 13, 0, ChatColor.GRAY + "Home: " + homeLocation.getBlockX() + ", " + homeLocation.getBlockZ());
                }

                tabList.updateSlot(player, 4, 1, ChatColor.RED + faction.getName());
                int count = 5;

                for (String member : this.getFactionOnlineUsers(faction)) {
                    tabList.updateSlot(player, count, 1, member);
                    ++count;
                }
            } else {
                tabList.updateSlot(player, 10, 0, ChatColor.GRAY + "None");
            }

            for (Event possibleEvent : EventManager.getInstance().getEvents()) {
                if (possibleEvent != null && possibleEvent instanceof KothEvent && possibleEvent.isActive()) {
                    KothEvent koth = (KothEvent) possibleEvent;
                    tabList.updateSlot(player, 14, 0, ChatColor.RED + "KOTH Info");
                    tabList.updateSlot(player, 15, 0, ChatColor.GRAY + "Name: " + ChatColor.YELLOW + koth.getName());
                    tabList.updateSlot(player, 16, 0, "" + ChatColor.GRAY + (FactionsPlugin.getInstance().isKitmapMode() ? this.getKitMapKothLocation(koth) : this.getKothLocation(koth)));
                }
            }

            if (profile.getOptions().getModifyTabList() == ProfileOptionsItemState.TAB_DETAILED_VANILLA) {
                tabList.updateSlot(player, 0, 2, ChatColor.RED + "Faction List");
                int count = 1;

                final Map<PlayerFaction, Integer> factionOnlineMap = new HashMap<PlayerFaction, Integer>();
                for (final Player target : PlayerUtility.getOnlinePlayers()) {
                    if (player.canSee(target)) {
                        final PlayerFaction playerFaction = Profile.getByUuid(target.getUniqueId()).getFaction();
                        if (playerFaction == null) {
                            continue;
                        }
                        factionOnlineMap.put(playerFaction, factionOnlineMap.getOrDefault(playerFaction, 0) + 1);
                    }
                }

                final List<Map.Entry<PlayerFaction, Integer>> sortedMap = (List<Map.Entry<PlayerFaction, Integer>>) MapSorting.sortedValues(factionOnlineMap, (Comparator) Comparator.reverseOrder());
                for (final Map.Entry<PlayerFaction, Integer> sortedEntry : sortedMap) {
                    tabList.updateSlot(player, count, 2, this.getFixedFactionName(player, sortedEntry.getKey()) + ChatColor.GRAY + " (" + sortedEntry.getValue() + ")");
                    ++count;
                }
            } else if (profile.getOptions().getModifyTabList() == ProfileOptionsItemState.TAB_DETAILED) {
                tabList.updateSlot(player, 0, 2, ChatColor.RED + "End Portals");
                tabList.updateSlot(player, 1, 2, ChatColor.GRAY + "1000, 1000");
                tabList.updateSlot(player, 2, 2, ChatColor.GRAY + "in each quadrant");

                tabList.updateSlot(player, 4, 2, ChatColor.RED + "Kit");
                tabList.updateSlot(player, 5, 2, ChatColor.GRAY + "Prot 1, Sharp 1");

                tabList.updateSlot(player, 7, 2, ChatColor.RED + "Border");
                tabList.updateSlot(player, 8, 2, ChatColor.GRAY + "3000");
            }
        }
    }

    private String getCardinalDirection(final Player player) {
        double rot = (player.getLocation().getYaw() - 90.0f) % 360.0f;
        if (rot < 0.0) {
            rot += 360.0;
        }
        return this.getDirection(rot);
    }

    private String getKothLocation(KothEvent event) {
        return (event.getZone().getCenter().getX() < 0 ? "-500" : "500") + ", " + event.getZone().getCenter().getBlockY() + ", " + (event.getZone().getCenter().getZ() < 0 ? "-500" : "500");
    }

    private String getKitMapKothLocation(KothEvent event) {
        return (event.getZone().getCenter().getX() < 0 ? "-250" : "250") + ", " + event.getZone().getCenter().getBlockY() + ", " + (event.getZone().getCenter().getZ() < 0 ? "-250" : "250");
    }

    private String getDirection(final double rot) {
        if (0.0 <= rot && rot < 22.5) {
            return "W";
        }
        if (22.5 <= rot && rot < 67.5) {
            return "NW";
        }
        if (67.5 <= rot && rot < 112.5) {
            return "N";
        }
        if (112.5 <= rot && rot < 157.5) {
            return "NE";
        }
        if (157.5 <= rot && rot < 202.5) {
            return "E";
        }
        if (202.5 <= rot && rot < 247.5) {
            return "SE";
        }
        if (247.5 <= rot && rot < 292.5) {
            return "S";
        }
        if (292.5 <= rot && rot < 337.5) {
            return "SW";
        }
        if (337.5 <= rot && rot < 360.0) {
            return "W";
        }
        return null;
    }

    private ChatColor getRelationWithPlayer(Player player, Player target) {

        PlayerFaction playerFaction = Profile.getByPlayer(player).getFaction();
        PlayerFaction targetFaction = Profile.getByPlayer(target).getFaction();

        if (player.getName().equalsIgnoreCase(target.getName())) {
            return ChatColor.DARK_GREEN;
        }

        if (playerFaction == null) {
            return ChatColor.YELLOW;
        }

        if (targetFaction == null) {
            return ChatColor.YELLOW;
        }

        if (playerFaction.equals(targetFaction)) {
            return ChatColor.DARK_GREEN;
        }

        if (playerFaction.getAllies().contains(targetFaction)) {
            return ChatColor.LIGHT_PURPLE;
        }

        if (Profile.getByPlayer(target).getCooldownByType(ProfileCooldownType.ARCHER_TAG) != null) {
            return ChatColor.RED;
        }

        if (playerFaction.getFocusPlayer() != null && playerFaction.getFocusPlayer() == target.getUniqueId()) {
            return ChatColor.DARK_RED;
        }

        return ChatColor.YELLOW;
    }

    private String getFixedFactionName(Player player, Faction faction) {

        if (faction == null) {
            return null;
        }

        if (faction instanceof SystemFaction) {
            return ((SystemFaction) faction).getColor() + faction.getName();
        }

        PlayerFaction playerFaction = Profile.getByPlayer(player).getFaction();

        if (playerFaction == null) {
            return ChatColor.RED + faction.getName();
        } else if (faction instanceof PlayerFaction && playerFaction.getAllies().contains(faction)) {
            return ChatColor.LIGHT_PURPLE + faction.getName();
        } else if (playerFaction.equals(faction)) {
            return ChatColor.GREEN + faction.getName();
        } else {
            return ChatColor.YELLOW + faction.getName();
        }
    }

    private List<String> getFactionOnlineUsers(PlayerFaction faction) {

        List<String> list = new ArrayList<>();

        if (faction != null) {

            Player leader = Bukkit.getPlayer(faction.getLeader());

            if (leader != null && leader.isOnline()) {
                list.add(ChatColor.DARK_GREEN + leader.getName() + ChatColor.GRAY + "**");
            }

            for (UUID officers : faction.getOfficers()) {
                Player offcier = Bukkit.getPlayer(officers);

                if (offcier != null && offcier.isOnline()) {
                    list.add(ChatColor.DARK_GREEN + offcier.getName() + ChatColor.GRAY + "*");
                }
            }

            for (Player members : faction.getNoRoleMembers()) {
                list.add(ChatColor.DARK_GREEN + members.getName());
            }
        }

        return list;
    }
}
