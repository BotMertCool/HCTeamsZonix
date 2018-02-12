package us.zonix.hcfactions.factions.claims;

import club.minemen.spigot.handler.MovementHandler;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import us.zonix.hcfactions.FactionsPlugin;
import us.zonix.hcfactions.factions.type.PlayerFaction;
import us.zonix.hcfactions.factions.type.SystemFaction;
import us.zonix.hcfactions.files.ConfigFile;
import us.zonix.hcfactions.profile.Profile;

public class CustomMovementHandler implements MovementHandler {

	private final FactionsPlugin plugin = FactionsPlugin.getInstance();
	private ConfigFile mainConfig = plugin.getMainConfig();
	private ConfigFile langConfig = plugin.getLanguageConfig();
	
	@Override
	public void handleUpdateLocation(Player p, Location to, Location from, PacketPlayInFlying packetPlayInFlying) {

		if (to.getX() != from.getX() || to.getZ() != from.getZ()) {
			final Profile profile = Profile.getByPlayer(p);
			final Claim claim = Claim.getProminentClaimAt(to);

			if (claim != null) {
				if (claim.isInside(to)) {
					if (profile.getLastInside() == null) {
						profile.setLastInside(claim);
						p.sendMessage(langConfig.getString("FACTION_CLAIM.LEAVE.WILDERNESS"));
						p.sendMessage(getEnteringMessage(profile, claim));
						return;
					}

					if (profile.getLastInside().getFaction() != claim.getFaction()) {
						if (profile.getLastInside().isInside(to) && !profile.getLastInside().isGreaterThan(claim)) {
							return;
						}

						p.sendMessage(getLeavingMessage(profile, profile.getLastInside()));
						p.sendMessage(getEnteringMessage(profile, claim));
					}

					profile.setLastInside(claim);
				}
				else {
					if (profile.getLastInside() != null && profile.getLastInside() == claim) {
						new BukkitRunnable() {
							@Override
							public void run() {
								if (profile.getLastInside() != null && profile.getLastInside() == claim) {
									p.sendMessage(getLeavingMessage(profile, claim));
									p.sendMessage(langConfig.getString("FACTION_CLAIM.ENTER.WILDERNESS"));
									profile.setLastInside(null);
								}
							}
						}.runTaskLater(plugin, 1L);
					}
				}
			}
			else {
				if (profile.getLastInside() != null) {
					p.sendMessage(getLeavingMessage(profile, profile.getLastInside()));
					p.sendMessage(langConfig.getString("FACTION_CLAIM.ENTER.WILDERNESS"));
					profile.setLastInside(profile.getLastInside());
					profile.setLastInside(null);
				}
			}

		}
	}

	@Override
	public void handleUpdateRotation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {

	}

	private String getLeavingMessage(Profile profile, Claim inside) {

		if (profile.getFaction() != null && profile.getFaction() == inside.getFaction()) {
			return langConfig.getString("FACTION_CLAIM.LEAVE.FRIENDLY").replace("%FACTION%", inside.getFaction().getName());
		}
		else if (profile.getFaction() != null && inside.getFaction() instanceof PlayerFaction && profile.getFaction().getAllies().contains(inside.getFaction())) {
			return langConfig.getString("FACTION_CLAIM.LEAVE.ALLY").replace("%FACTION%", inside.getFaction().getName());
		}
		else if (!(inside.getFaction() instanceof PlayerFaction)) {
			SystemFaction systemFaction = (SystemFaction) inside.getFaction();

			if (systemFaction.isDeathban()) {
				return langConfig.getString("FACTION_CLAIM.LEAVE.SYSTEM_FACTION_DEATHBAN").replace("%FACTION%", systemFaction.getName()).replace("%COLOR%", systemFaction.getColor() + "");
			}
			else {
				return langConfig.getString("FACTION_CLAIM.LEAVE.SYSTEM_FACTION_NON-DEATHBAN").replace("%FACTION%", systemFaction.getName()).replace("%COLOR%", systemFaction.getColor() + "");
			}
		}
		else {
			return langConfig.getString("FACTION_CLAIM.LEAVE.ENEMY").replace("%FACTION%", inside.getFaction().getName());
		}
	}

	private String getEnteringMessage(Profile profile, Claim inside) {
		if (profile.getFaction() != null && profile.getFaction() == inside.getFaction()) {
			return langConfig.getString("FACTION_CLAIM.ENTER.FRIENDLY").replace("%FACTION%", inside.getFaction().getName());
		}
		else if (profile.getFaction() != null && inside.getFaction() instanceof PlayerFaction && profile.getFaction().getAllies().contains(inside.getFaction())) {
			return langConfig.getString("FACTION_CLAIM.ENTER.ALLY").replace("%FACTION%", inside.getFaction().getName());
		}
		else if (!(inside.getFaction() instanceof PlayerFaction)) {
			SystemFaction systemFaction = (SystemFaction) inside.getFaction();

			if (systemFaction.isDeathban()) {
				return langConfig.getString("FACTION_CLAIM.ENTER.SYSTEM_FACTION_DEATHBAN").replace("%FACTION%", systemFaction.getName()).replace("%COLOR%", systemFaction.getColor() + "");
			}
			else {
				Player player = Bukkit.getPlayer(profile.getUuid());

				if (player != null) {
					player.setHealth(player.getMaxHealth());
					player.setFoodLevel(20);
					player.setFireTicks(0);
					player.setSaturation(4.0F);
				}

				return langConfig.getString("FACTION_CLAIM.ENTER.SYSTEM_FACTION_NON-DEATHBAN").replace("%FACTION%", systemFaction.getName()).replace("%COLOR%", systemFaction.getColor() + "");
			}
		}
		else {
			return langConfig.getString("FACTION_CLAIM.ENTER.ENEMY").replace("%FACTION%", inside.getFaction().getName());
		}
	}
}
