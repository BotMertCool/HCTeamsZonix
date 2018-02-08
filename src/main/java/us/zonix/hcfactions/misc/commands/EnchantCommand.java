package us.zonix.hcfactions.misc.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.zonix.hcfactions.util.ItemBuilder;
import us.zonix.hcfactions.util.PluginCommand;
import us.zonix.hcfactions.util.command.Command;
import us.zonix.hcfactions.util.command.CommandArgs;

public class EnchantCommand extends PluginCommand {

    @Command(name = "enchant", permission = "commands.enchant")
    public void onCommand(final CommandArgs command) {
        final Player player = (Player)command.getSender();
        if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You have nothing in your hand...");
            return;
        }
        if (command.getArgs().length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /enchant <enchant> <level>");
            return;
        }

        Enchantment enchantment;

        try {
            enchantment = Enchantment.getByName(command.getArgs(0).toUpperCase());
        } catch (Exception ex) {
            enchantment = null;
        }

        if(enchantment == null) {
            player.sendMessage(ChatColor.RED + "You did not supply a valid enchantment.");
            return;
        }

        ItemBuilder builder = new ItemBuilder(player.getItemInHand().clone()).enchantment(enchantment, Integer.parseInt(command.getArgs(1)));
        player.getInventory().setItemInHand(builder.build());
        player.updateInventory();
        player.sendMessage(ChatColor.YELLOW + "You have applied the enchantment.");
    }
}
