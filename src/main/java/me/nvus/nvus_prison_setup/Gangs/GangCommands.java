package me.nvus.nvus_prison_setup.Gangs;

import me.nvus.nvus_prison_setup.Database.DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;

public class GangCommands implements CommandExecutor {

    private final GangManager gangManager;

    public GangCommands(DatabaseManager dbManager) {
        this.gangManager = new GangManager(dbManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUuid = player.getUniqueId();

        if (args.length >= 2 && args[0].equalsIgnoreCase("gang")) {
            switch (args[1].toLowerCase()) {
                case "invite":
                    return handleGangInvite(sender, args);
                case "accept":
                    // Dynamically determine the gangName the player is invited to join
                    return handleGangAccept(player, playerUuid);
                case "deny":
                    // Dynamically determine the gangName the player wants to deny
                    return handleGangDeny(player, playerUuid);
                case "leave":
                    return handleGangLeave(player, playerUuid);
                default:
                    player.sendMessage("Invalid gang command.");
                    return true;
            }
        }
        return false;
    }

    private boolean handleGangInvite(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage("Usage: /nvus gang invite <playerName> <gangName>");
            return true;
        }

        Player player = (Player) sender;
        String targetPlayerName = args[1];
        String gangName = args[2];
        Player targetPlayer = sender.getServer().getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            sender.sendMessage("Could not find player: " + targetPlayerName);
            return true;
        }

        UUID targetPlayerUuid = targetPlayer.getUniqueId();
        if (!gangManager.canInvite(player.getUniqueId())) {
            sender.sendMessage("You do not have permission to invite players to this gang.");
            return true;
        }

        boolean invited = gangManager.addInvitation(targetPlayerUuid, gangName);
        sender.sendMessage(invited ? "Player invited to gang successfully." : "Failed to invite player to gang.");
        return true;
    }

    private boolean handleGangAccept(Player player, UUID playerUuid) {
        String gangName = gangManager.getCurrentGangName(playerUuid); // Method in GangManager to determine the gangName
        if (gangName == null) {
            player.sendMessage("You don't have any pending invitations.");
            return true;
        }
        if (gangManager.acceptInvitation(playerUuid, gangName)) {
            player.sendMessage("You have successfully joined the gang: " + gangName + "!");
        } else {
            player.sendMessage("Failed to join the gang.");
        }
        return true;
    }

    private boolean handleGangDeny(Player player, UUID playerUuid) {
        String gangName = gangManager.getCurrentGangName(playerUuid); // Similar method as accept to find the gangName
        if (gangName == null) {
            player.sendMessage("You don't have any pending invitations to deny.");
            return true;
        }
        if (gangManager.denyInvitation(playerUuid, gangName)) {
            player.sendMessage("You have successfully denied the gang invitation from: " + gangName + ".");
        } else {
            player.sendMessage("Failed to deny the gang invitation.");
        }
        return true;
    }

    private boolean handleGangLeave(Player player, UUID playerUuid) {
        String gangName = gangManager.getCurrentGangName(playerUuid); // Method to get current gang of the player
        if (gangName == null) {
            player.sendMessage("You are not part of any gang!");
            return true;
        }
        if (gangManager.leaveGang(playerUuid, gangName)) {
            player.sendMessage("You have successfully left the gang: " + gangName + ".");
        } else {
            player.sendMessage("Failed to leave the gang.");
        }
        return true;
    }
}
