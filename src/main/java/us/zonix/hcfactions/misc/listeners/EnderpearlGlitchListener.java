package us.zonix.hcfactions.misc.listeners;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnderpearlGlitchListener implements Listener {

    private static List<Material> allowed = new ArrayList();
    private ImmutableSet<Material> blockedPearlTypes = Sets.immutableEnumSet(Material.THIN_GLASS, Material.IRON_FENCE, Material.FENCE, Material.NETHER_FENCE, Material.FENCE_GATE,
            Material.ACACIA_STAIRS, Material.BIRCH_WOOD_STAIRS, Material.BRICK_STAIRS, Material.COBBLESTONE_STAIRS, Material.DARK_OAK_STAIRS, Material.JUNGLE_WOOD_STAIRS,
            Material.NETHER_BRICK_STAIRS, Material.QUARTZ_STAIRS, Material.SANDSTONE_STAIRS, Material.SMOOTH_STAIRS, Material.SPRUCE_WOOD_STAIRS, Material.WOOD_STAIRS);

    static {
        allowed.add(Material.SIGN);
        allowed.add(Material.SIGN_POST);
        allowed.add(Material.WALL_SIGN);
        allowed.add(Material.SUGAR_CANE_BLOCK);
        allowed.add(Material.WHEAT);
        allowed.add(Material.POTATO);
        allowed.add(Material.CARROT);
        allowed.add(Material.STEP);
        allowed.add(Material.AIR);
        allowed.add(Material.WOOD_STEP);
        allowed.add(Material.SOUL_SAND);
        allowed.add(Material.CARPET);
        allowed.add(Material.STONE_PLATE);
        allowed.add(Material.WOOD_PLATE);
        allowed.add(Material.LADDER);
        allowed.add(Material.CHEST);
        allowed.add(Material.WATER);
        allowed.add(Material.STATIONARY_WATER);
        allowed.add(Material.LAVA);
        allowed.add(Material.STATIONARY_LAVA);
        allowed.add(Material.REDSTONE_COMPARATOR);
        allowed.add(Material.REDSTONE_COMPARATOR_OFF);
        allowed.add(Material.REDSTONE_COMPARATOR_ON);
        allowed.add(Material.IRON_PLATE);
        allowed.add(Material.GOLD_PLATE);
        allowed.add(Material.DAYLIGHT_DETECTOR);
        allowed.add(Material.STONE_BUTTON);
        allowed.add(Material.WOOD_BUTTON);
        allowed.add(Material.HOPPER);
        allowed.add(Material.RAILS);
        allowed.add(Material.ACTIVATOR_RAIL);
        allowed.add(Material.DETECTOR_RAIL);
        allowed.add(Material.POWERED_RAIL);
        allowed.add(Material.TRIPWIRE_HOOK);
        allowed.add(Material.TRIPWIRE);
        allowed.add(Material.SNOW_BLOCK);
        allowed.add(Material.REDSTONE_TORCH_OFF);
        allowed.add(Material.REDSTONE_TORCH_ON);
        allowed.add(Material.DIODE_BLOCK_OFF);
        allowed.add(Material.DIODE_BLOCK_ON);
        allowed.add(Material.DIODE);
        allowed.add(Material.SEEDS);
        allowed.add(Material.MELON_SEEDS);
        allowed.add(Material.PUMPKIN_SEEDS);
        allowed.add(Material.DOUBLE_PLANT);
        allowed.add(Material.LONG_GRASS);
        allowed.add(Material.WEB);;
        allowed.add(Material.SNOW);
        allowed.add(Material.FLOWER_POT);
        allowed.add(Material.BREWING_STAND);
        allowed.add(Material.CAULDRON);
        allowed.add(Material.CACTUS);
        allowed.add(Material.WATER_LILY);
        allowed.add(Material.RED_ROSE);
        allowed.add(Material.ENCHANTMENT_TABLE);
        allowed.add(Material.ENDER_PORTAL_FRAME);
        allowed.add(Material.PORTAL);
        allowed.add(Material.ENDER_PORTAL);
        allowed.add(Material.ENDER_CHEST);
        allowed.add(Material.NETHER_FENCE);
        allowed.add(Material.NETHER_WARTS);
        allowed.add(Material.REDSTONE_WIRE);
        allowed.add(Material.LEVER);
        allowed.add(Material.YELLOW_FLOWER);
        allowed.add(Material.CROPS);
        allowed.add(Material.WATER);
        allowed.add(Material.LAVA);
        allowed.add(Material.SKULL);
        allowed.add(Material.TRAPPED_CHEST);
        allowed.add(Material.FIRE);
        allowed.add(Material.BROWN_MUSHROOM);
        allowed.add(Material.RED_MUSHROOM);
        allowed.add(Material.DEAD_BUSH);
        allowed.add(Material.SAPLING);
        allowed.add(Material.TORCH);
        allowed.add(Material.MELON_STEM);
        allowed.add(Material.PUMPKIN_STEM);
        allowed.add(Material.COCOA);
        allowed.add(Material.BED);
        allowed.add(Material.BED_BLOCK);
        allowed.add(Material.PISTON_EXTENSION);
        allowed.add(Material.PISTON_MOVING_PIECE);

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.hasItem() && event.getItem().getType() == Material.ENDER_PEARL) {
            Block block = event.getClickedBlock();
            if (block.getType().isSolid() && this.blockedPearlTypes.contains(block.getType()) && !(block.getState() instanceof InventoryHolder)) {

                event.setCancelled(true);

                Player player = event.getPlayer();
                player.sendMessage(ChatColor.RED + "Pearl glitching is not allowed.");
                player.updateInventory();
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPearlClip(PlayerTeleportEvent event) {

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            Location to = event.getTo();
            if (blockedPearlTypes.contains(to.getBlock().getType()) && to.getBlock().getType() != Material.FENCE_GATE && to.getBlock().getType() != Material.TRAP_DOOR) {

                Player player = event.getPlayer();
                player.sendMessage(ChatColor.RED + "Pearl glitching is not allowed.");
                event.setCancelled(true);
                return;
            }

            to.setX(to.getBlockX() + 0.5);
            to.setZ(to.getBlockZ() + 0.5);
            if((!allowed.contains(to.getBlock()) || !allowed.contains(to.clone().add(0.0D, 1.0D, 0.0D).getBlock())) && (to.getBlock().getType().isSolid() || to.clone().add(0.0D, 1.0D, 0.0D).getBlock().getType().isSolid()) && to.clone().subtract(0.0D, 1.0D, 0.0D).getBlock().getType().isSolid()) {
                Player player = event.getPlayer();
                player.sendMessage(ChatColor.RED + "Pearl couldn't find a safe location, cancelling.");
                event.setCancelled(true);
            } else  if(!allowed.contains(to.clone().add(0.0D, 1.0D, 0.0D).getBlock()) && to.clone().add(0.0D, 1.0D, 0.0D).getBlock().getType().isSolid() && !to.getBlock().getType().isSolid()){
                to.setY(to.getY() - 0.7);
            }

            event.setTo(to);
        }
    }


}
