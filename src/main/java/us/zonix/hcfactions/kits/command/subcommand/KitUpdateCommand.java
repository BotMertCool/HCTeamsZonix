package us.zonix.hcfactions.kits.command.subcommand;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import us.zonix.hcfactions.kits.Kit;
import us.zonix.hcfactions.kits.command.KitCommand;
import us.zonix.hcfactions.util.PluginCommand;
import us.zonix.hcfactions.util.command.Command;
import us.zonix.hcfactions.util.command.CommandArgs;
import us.zonix.hcfactions.util.command.CommandArgs;

import java.util.Arrays;

public class KitUpdateCommand extends PluginCommand {

    @Command(name = "kit.update", permission = "kit.admin")
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /kit update <name>");
            return;
        }

        String name = StringUtils.join(args);
        Kit kit = Kit.getByName(name);

        if (kit == null) {
            player.sendMessage(ChatColor.RED + "A kit named '" + name + "' does not exist.");
            return;
        }

        KitCommand.openUpdateInventory(player, kit);
    }
}
