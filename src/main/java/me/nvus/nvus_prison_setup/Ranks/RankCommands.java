package me.nvus.nvus_prison_setup.Ranks;

import me.nvus.nvus_prison_setup.PrisonSetup;
import me.nvus.nvus_prison_setup.Ranks.RankManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommands implements CommandExecutor {

    private final RankManager rankManager;

    public RankCommands(PrisonSetup plugin) {
        this.rankManager = plugin.getRankManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        Player player = (Player) sender;

        switch (label.toLowerCase()) {
            case "rankup":
                return handleRankUpCommand(player);
            case "ranks":
                return handleRanksCommand(player);
            default:
                return false;
        }
    }

    private boolean handleRankUpCommand(Player player) {
        // Check if the player has a rank, assign the default rank if not
        Rank currentRank = rankManager.getCurrentRank(player);
        if (currentRank == null) {
            rankManager.assignDefaultRank(player);
            player.sendMessage(ChatColor.YELLOW + "We couldn't find you in the ranks database. Assigning default rank now!");
            return true;
        }

        boolean success = rankManager.rankUp(player);
        if (!success) {
            player.sendMessage(ChatColor.RED + "Unable to rank up. Please check your rank and balance using /ranks");
        }
        return true;
    }

    private boolean handleRanksCommand(Player player) {
        String ranksMessage = rankManager.getRanksDisplay(player);
        player.sendMessage(ranksMessage);
        return true;
    }
}
