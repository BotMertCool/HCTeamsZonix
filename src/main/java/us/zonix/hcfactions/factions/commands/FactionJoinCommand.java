package us.zonix.hcfactions.factions.commands;

import us.zonix.core.rank.Rank;
import us.zonix.hcfactions.factions.Faction;
import us.zonix.hcfactions.factions.events.player.PlayerJoinFactionEvent;
import us.zonix.hcfactions.factions.type.PlayerFaction;
import us.zonix.hcfactions.profile.Profile;
import us.zonix.hcfactions.util.command.Command;
import us.zonix.hcfactions.util.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.zonix.hcfactions.profile.Profile;
import us.zonix.hcfactions.util.command.CommandArgs;

/**
 * Copyright 2016 Alexander Maxwell
 * Use and or redistribution of compiled JAR file and or source code is permitted only if given
 * explicit permission from original author: Alexander Maxwell
 */
public class FactionJoinCommand extends FactionCommand {
    @Command(name = "f.join", aliases = {"faction.join", "factions.join", "f.accept", "factions.accept", "faction.accept"})
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        if (command.getArgs().length == 0) {
            player.sendMessage(langConfig.getString("TOO_FEW_ARGS.JOIN"));
            return;
        }

        Profile profile = Profile.getByPlayer(player);

        if (profile.getFaction() != null) {
            player.sendMessage(langConfig.getString("ERROR.ALREADY_IN_FACTION"));
            return;
        }

        String factionName = command.getArgs(0);
        Faction faction = Faction.getByName(factionName);
        PlayerFaction playerFaction = null;

        if (faction instanceof PlayerFaction) {
            playerFaction = (PlayerFaction) faction;
        }

        if (faction == null || (!(faction instanceof PlayerFaction) || (!(playerFaction.getInvitedPlayers().containsKey(player.getUniqueId()))))) {
            playerFaction = PlayerFaction.getByPlayerName(factionName);

            if (playerFaction == null || !(playerFaction.getInvitedPlayers().containsKey(player.getUniqueId())) && !us.zonix.core.profile.Profile.getByUuid(player.getUniqueId()).getRank().isAboveOrEqual(Rank.DEVELOPER)) {
                player.sendMessage(langConfig.getString("ERROR.NOT_INVITED"));
                return;
            }
        }

        if (playerFaction.getAllPlayerUuids().size() >= mainConfig.getInt("FACTION_GENERAL.MAX_PLAYERS")) {
            player.sendMessage(langConfig.getString("ERROR.MAX_PLAYERS").replace("%FACTION%", playerFaction.getName()));
            return;
        }

        player.sendMessage(langConfig.getString("FACTION_OTHER.JOINED").replace("%FACTION%", playerFaction.getName()));
        playerFaction.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION.PLAYER_JOINED").replace("%PLAYER%", player.getName()));

        playerFaction.getInvitedPlayers().remove(player.getUniqueId());
        playerFaction.getMembers().add(player.getUniqueId());
        profile.setFaction(playerFaction);

        Bukkit.getPluginManager().callEvent(new PlayerJoinFactionEvent(player, playerFaction));
    }
}
