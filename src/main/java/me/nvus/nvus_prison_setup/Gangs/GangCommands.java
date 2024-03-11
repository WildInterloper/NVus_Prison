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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }

        Player player = (Player) sender;

        // Display help if no arguments are provided or if the first argument is "help"
        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
            sendGangCommandHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                return handleGangCreate(player, args);
            case "invite":
                return handleGangInvite(sender, args);
            case "accept":
                return handleGangAccept(player, player.getUniqueId());
            case "deny":
                return handleGangDeny(player, player.getUniqueId());
            case "leave":
                return handleGangLeave(player, player.getUniqueId());
            case "promote":
                return handleGangPromote(sender, args);
            case "demote":
                return handleGangDemote(sender, args);
            case "kick":
                return handleGangKick(sender, args);
            case "disband":
                return handleGangDisband(sender, args);
            default:
                player.sendMessage(ChatColor.RED + "Invalid gang command. Use /gang help for a list of commands.");
                return true;
        }
    }

    private boolean handleGangPromote(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /gang promote <player>");
            return true;
        }

        Player player = (Player) sender;
        Player targetPlayer = player.getServer().getPlayer(args[2]);
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + args[2]);
            return true;
        }

        String gangName = gangManager.getCurrentGangName(player.getUniqueId());
        if (gangName == null) {
            player.sendMessage(ChatColor.RED + "You are not in a gang.");
            return true;
        }

        boolean success = gangManager.promoteMember(player.getUniqueId(), targetPlayer.getUniqueId(), gangName);
        if (success) {
            player.sendMessage(ChatColor.GREEN + "Successfully promoted " + targetPlayer.getName() + ".");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to promote " + targetPlayer.getName() + ".");
        }

        return true;
    }

    private boolean handleGangDemote(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /gang demote <playerName>");
            return true;
        }

        String targetPlayerName = args[2];
        Player targetPlayer = player.getServer().getPlayer(targetPlayerName);

        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "Player " + targetPlayerName + " not found.");
            return true;
        }

        UUID playerUuid = player.getUniqueId();
        UUID targetPlayerUuid = targetPlayer.getUniqueId();
        String gangName = gangManager.getCurrentGangName(playerUuid);

        if (gangName == null) {
            player.sendMessage(ChatColor.RED + "You are not part of a gang.");
            return true;
        }

        // Check if the player has the authority to demote members in the gang
        if (!gangManager.canKickOrPromote(playerUuid, gangName)) {
            player.sendMessage(ChatColor.RED + "You do not have the permission to demote members in the gang.");
            return true;
        }

        // Attempt to demote the member
        boolean success = gangManager.demoteMember(playerUuid, targetPlayerUuid, gangName);
        if (success) {
            player.sendMessage(ChatColor.GREEN + "Successfully demoted " + targetPlayer.getName() + " in the gang.");
            targetPlayer.sendMessage(ChatColor.YELLOW + "You have been demoted in the gang " + gangName + ".");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to demote " + targetPlayer.getName() + ".");
        }

        return true;
    }

    private boolean handleGangKick(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /gang kick <player>");
            return true;
        }

        Player player = (Player) sender;
        Player targetPlayer = player.getServer().getPlayer(args[2]);
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + args[2]);
            return true;
        }

        String gangName = gangManager.getCurrentGangName(player.getUniqueId());
        if (gangName == null) {
            player.sendMessage(ChatColor.RED + "You are not in a gang.");
            return true;
        }

        boolean success = gangManager.kickMember(player.getUniqueId(), targetPlayer.getUniqueId(), gangName);
        if (success) {
            player.sendMessage(ChatColor.GREEN + "Successfully kicked " + targetPlayer.getName() + " from the gang.");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to kick " + targetPlayer.getName() + ".");
        }

        return true;
    }

    private boolean handleGangDisband(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        String gangName = gangManager.getCurrentGangName(player.getUniqueId());

        if (gangName == null) {
            player.sendMessage(ChatColor.RED + "You are not in a gang.");
            return true;
        }

        if (gangManager.disbandGang(gangName, player.getUniqueId())) {
            player.sendMessage(ChatColor.GREEN + "Your gang has been successfully disbanded.");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to disband your gang. You must be the gang owner.");
        }

        return true;
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
        // Check if the player has the required permission
        if (!player.hasPermission("nvus.gang.create")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to create a gang.");
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /gang create <gangName>");
            return true;
        }

        String gangName = args[1];

        // Check if the player already belongs to a gang
        String currentGang = gangManager.getCurrentGangName(player.getUniqueId());
        if (currentGang != null) {
            player.sendMessage(ChatColor.RED + "You already belong to a gang.");
            return true;
        }

        // Create the gang
        if (gangManager.createGang(gangName, player)) {
            player.sendMessage(ChatColor.GREEN + "Gang created successfully!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to create gang. The gang may already exist.");
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
