package me.nvus.nvus_prison_setup.AutoSell;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class EconomyHandler {

    private Economy economy;
    private MultiplierManager multiplierManager;

    public EconomyHandler(Economy economy, MultiplierManager multiplierManager) {
        this.economy = economy;
        this.multiplierManager = multiplierManager;
    }

    public void giveMoney(Player player, double amount) {
        double multiplier = multiplierManager.getPlayerMultiplier(player.getUniqueId());
        economy.depositPlayer(player, amount * multiplier);
    }
}

