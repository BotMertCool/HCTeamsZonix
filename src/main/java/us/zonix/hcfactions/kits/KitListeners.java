package us.zonix.hcfactions.kits;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import us.zonix.hcfactions.FactionsPlugin;
import us.zonix.hcfactions.crate.Crate;
import us.zonix.hcfactions.factions.Faction;
import us.zonix.hcfactions.kits.command.KitCommand;
import us.zonix.hcfactions.profile.Profile;
import us.zonix.hcfactions.profile.kit.ProfileKit;
import us.zonix.hcfactions.profile.kit.ProfileKitEnergy;
import us.zonix.hcfactions.util.ItemBuilder;
import us.zonix.hcfactions.FactionsPlugin;
import us.zonix.hcfactions.profile.Profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class KitListeners implements Listener {

    private HashMap<UUID, Long> cooldown = new HashMap<>();

    @EventHandler
    public void onSignChange(final SignChangeEvent event) {
        final Player player = event.getPlayer();
        if (!player.hasPermission("kits.admin") && !player.isOp()) {
            return;
        }
        if (event.getLines().length < 2) {
            return;
        }
        if (event.getLine(0).equalsIgnoreCase("[Class]")) {
            final Kit kit = Kit.getByName(event.getLine(1));
            if (kit == null) {
                player.sendMessage(ChatColor.RED + "That kit does not exist.");
                return;
            }
            event.setLine(0, "§c[Class]");
            event.setLine(1, kit.getName());
        }
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!(event.getClickedBlock().getState() instanceof Sign)) {
            return;
        }
        final Sign sign = (Sign)event.getClickedBlock().getState();

        if (!sign.getLine(0).equalsIgnoreCase("§c[Class]")) {
            return;
        }

        event.setCancelled(true);

        final Kit kit = Kit.getByName(ChatColor.stripColor(sign.getLine(1)));

        if (kit == null) {
            return;
        }
        final Player player = event.getPlayer();

        if(this.cooldown.containsKey(player.getUniqueId()) && cooldown.get(player.getUniqueId()) > System.currentTimeMillis()) {
            player.sendMessage(ChatColor.RED + "You must wait before you load another kit.");
            return;
        }


        kit.loadFullKit(player);

        this.cooldown.put(player.getUniqueId(), System.currentTimeMillis() + 10 * 1000L);
    }

    @EventHandler
    public void onInventoryMenuClickEvent(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (event.getInventory().getTitle().contains("Preview")) {

            event.setCancelled(true);

            if (event.getCurrentItem() == null) {
                return;
            }

            if (!event.getCurrentItem().hasItemMeta()) {
                return;
            }

            if (!event.getCurrentItem().getItemMeta().hasDisplayName()) {
                return;
            }

            if (event.getCurrentItem().getType() == Material.ARROW) {
                KitCommand.openKitsInventory(player);
            }
        }
    }

    @EventHandler
    public void onInventorySelectClickEvent(InventoryClickEvent event) {

        if(!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (event.getInventory().getTitle().equalsIgnoreCase("HCF Kits")) {

            event.setCancelled(true);

            if(event.getCurrentItem() == null) {
                return;
            }

            if(!event.getCurrentItem().hasItemMeta()) {
                return;
            }

            if(!event.getCurrentItem().getItemMeta().hasDisplayName()) {
                return;
            }

            if(event.getCurrentItem().getType() == Material.STAINED_GLASS_PANE && event.getCurrentItem().getDurability() != 7) {

                String kitName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).split(": ")[1];
                Kit kit = Kit.getByName(kitName);

                if(kit != null) {

                    if(event.getClick() == ClickType.RIGHT) {
                        KitCommand.openPreviewInventory(player, kit);
                    } else if(event.getClick() == ClickType.LEFT) {

                        player.closeInventory();

                        if (!player.hasPermission("class.use." + kit.getName().toLowerCase())) {
                            player.sendMessage(ChatColor.YELLOW + "Purchase HCF Kits @ store.zonix.us");
                            return;
                        }

                        Profile profile = Profile.getByUuid(player.getUniqueId());

                        if (profile == null) {
                            return;
                        }

                        if (KitCommand.isCooldownActive(profile, kit)) {
                            player.sendMessage(FactionsPlugin.getInstance().getLanguageConfig().getString("KIT_COMMAND.DELAY").replace("%TIME%", KitCommand.getTimeLeft(profile, kit)));
                            return;
                        }


                        kit.loadKit(player);
                        profile.getKitDelay().put(kit, System.currentTimeMillis() + 172800 * 1000L);

                        player.sendMessage(FactionsPlugin.getInstance().getLanguageConfig().getString("KIT_COMMAND.SUCCESS").replace("%KIT%", kit.getName()));
                    }
                }
            }
        }

    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        if (inventory.getTitle().contains("Kits - 1/1") &&  player.hasPermission("kits.admin")) {
            Kit kit = Kit.getByName(ChatColor.stripColor(inventory.getItem(4).getItemMeta().getLore().get(0).replace("Kit: ", "")));
            if (kit != null) {
                List<ItemStack> toAdd = new ArrayList<>();

                for (int i = 9; i < inventory.getSize(); i++) {
                    ItemStack itemStack = inventory.getItem(i);
                    if (itemStack != null && itemStack.getType() != Material.AIR) {
                        toAdd.add(itemStack);
                    }
                }

                kit.getItems().clear();
                kit.getItems().addAll(toAdd);
            }
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        if (inventory.getTitle().contains("Kits - 1/1") &&  player.hasPermission("kits.admin")) {
            if (event.getRawSlot() <= 8) {
                event.setCancelled(true);
            }

            if (event.getClick().name().contains("SHIFT")) {
                ItemStack itemStack = event.getCurrentItem();
                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    Inventory clickedInventory = event.getClickedInventory();

                    if (clickedInventory != null && clickedInventory.contains(itemStack) && clickedInventory.equals(inventory)) {
                        return;
                    }

                    event.setCancelled(true);
                    player.getInventory().removeItem(itemStack);

                    int position = 0;
                    for (int i = 0; i < inventory.getSize(); i++) {
                        if (i > 8) {
                            ItemStack slot = inventory.getItem(i);
                            if (slot == null || slot.getType() == Material.AIR) {
                                position = i;
                                break;
                            }
                        }
                    }

                    inventory.setItem(position, itemStack);
                }
            }
        }
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        this.cooldown.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onKick(final PlayerKickEvent event) {
        this.cooldown.remove(event.getPlayer().getUniqueId());
    }
}
