package me.nvus.nvus_prison_setup.Ranks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public class RankListener implements Listener {
    private RankManager rankManager;

    public RankListener(RankManager rankManager) {
        this.rankManager = rankManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        handleNewPlayer(player);
//        if (rankManager.getCurrentRank(player) == null) {
//            // Assign to the default rank
//            rankManager.assignDefaultRank(player);
//        }
    }

    private void handleNewPlayer(Player player) {
        Rank currentRank = rankManager.getCurrentRank(player);
        if (currentRank == null) {
            rankManager.assignDefaultRank(player);
        }

    }
}
