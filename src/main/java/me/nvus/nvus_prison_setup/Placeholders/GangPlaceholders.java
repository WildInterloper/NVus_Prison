package me.nvus.nvus_prison_setup.Placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import me.nvus.nvus_prison_setup.Gangs.GangManager;
import me.nvus.nvus_prison_setup.Gangs.GangInfo;

public class GangPlaceholders extends PlaceholderExpansion {

    private GangManager gangManager;

    public GangPlaceholders(GangManager gangManager) {
        this.gangManager = gangManager;
    }

    @Override
    public String getIdentifier() {
        return "nvus";
    }

    @Override
    public String getAuthor() {
        return "never2nv";
    }

    @Override
    public String getVersion() {
        return "1.1";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        String gangName = gangManager.getCurrentGangName(player.getUniqueId());
        if (gangName == null) {
            return "No Gang";
        }

        GangInfo gangInfo = gangManager.getGangInfo(gangName);
        if (gangInfo == null) {
            return "Gang information could not be retrieved.";
        }

        switch (identifier) {
            case "gang_name":
                return gangInfo.getName();
            case "gang_owner":
                return gangInfo.getOwnerName();
            case "gang_members":
                return String.valueOf(gangInfo.getMemberCount());
            default:
                return null;
        }
    }
}
