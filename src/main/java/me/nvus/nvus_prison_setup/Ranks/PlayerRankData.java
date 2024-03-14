package me.nvus.nvus_prison_setup.Ranks;

import java.util.UUID;

public class PlayerRankData {
    private UUID uuid;
    private String username;
    private String rankName;

    public PlayerRankData(UUID uuid, String username, String rankName) {
        this.uuid = uuid;
        this.username = username;
        this.rankName = rankName;
    }

    // Getters
    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getRankName() {
        return rankName;
    }


}

