package me.nvus.nvus_prison_setup.Gangs;

import me.nvus.nvus_prison_setup.Database.DatabaseManager;
import org.bukkit.ChatColor;
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

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUuid = player.getUniqueId();

        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("gang"))) {
            // No subcommand specified, or only "gang" was specified
            sendGangCommandHelp(player);
            return true;
        } else if (args.length >= 1 && args[0].equalsIgnoreCase("gang")) {
            // A subcommand is specified, handle it accordingly
            switch (args[1].toLowerCase()) {
                case "create":
                    return handleGangCreate(player, args);
                case "invite":
                    return handleGangInvite(sender, args);
                case "accept":
                    return handleGangAccept(player, playerUuid);
                case "deny":
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

    private void sendGangCommandHelp(Player player) {
        StringBuilder message = new StringBuilder();
        message.append(ChatColor.GREEN).append("\n");
        message.append(ChatColor.LIGHT_PURPLE).append("NVus Prison Gangs:\n");
        message.append(ChatColor.DARK_GRAY).append("=======\n");
        message.append(ChatColor.GREEN).append("/gang create <name/tag> - Use this to create a gang.\n");
        message.append(ChatColor.GREEN).append("/gang invite <player> - Invite player to your gang.\n");
        message.append(ChatColor.GREEN).append("/gang accept - Accept an invite to a gang.\n");
        message.append(ChatColor.GREEN).append("/gang deny - Deny an invite to a gang.\n");
        message.append(ChatColor.GREEN).append("/gang leave - Leave your current gang.\n");
        message.append(ChatColor.GREEN).append("\n");
        message.append(ChatColor.YELLOW).append("COMING SOON:\n");
        message.append(ChatColor.YELLOW).append("=============\n");
        message.append(ChatColor.GREEN).append("/gang disband - Delete/Remove your gang.\n");
        message.append(ChatColor.GREEN).append("/gang promote <player> - Promote a gang member to a higher rank.\n");
        message.append(ChatColor.GREEN).append("/gang kick <player> - Kick a member from your gang.\n");
        message.append(ChatColor.GREEN).append("\n");

        player.sendMessage(message.toString());
    }


    private boolean handleGangCreate(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage("Usage: /gang create <gangName>");
            return true;
        }

        String gangName = args[1];

        // Check if the player already belongs to a gang
        String currentGang = gangManager.getCurrentGangName(player.getUniqueId());
        if (currentGang != null) {
            player.sendMessage("You already belong to a gang.");
            return true;
        }

        // Create the gang
        if (gangManager.createGang(gangName, player)) {
            player.sendMessage("Gang created successfully!");
        } else {
            player.sendMessage("Failed to create gang. The gang may already exist.");
        }
        return true;
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
