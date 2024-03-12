package me.nvus.nvus_prison_setup.Placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import me.nvus.nvus_prison_setup.Gangs.GangManager;

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
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        // Placeholder: %nvus_gang_name%
        if (identifier.equals("gang_name")) {
            String gangName = gangManager.getCurrentGangName(player.getUniqueId());
            return gangName != null ? gangName : "No Gang";
        }

        return null;
    }
}