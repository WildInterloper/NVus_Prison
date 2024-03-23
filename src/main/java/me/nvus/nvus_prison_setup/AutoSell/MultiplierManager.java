package me.nvus.nvus_prison_setup.AutoSell;
import me.nvus.nvus_prison_setup.PrisonSetup;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MultiplierManager implements CommandExecutor {

    private final PrisonSetup plugin;
    private final Map<UUID, Double> multipliers = new HashMap<>();

    // Store the scheduled tasks for removing multipliers
    private final Map<UUID, BukkitTask> removalTasks = new HashMap<>();

    public MultiplierManager(PrisonSetup plugin) {
        this.plugin = plugin; // Store the passed plugin instance for later use
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("multiplier.use")) {
            sender.sendMessage("You don't have permission to use this command.");
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage("Usage: /multiplier <player> <multiplier> <duration in minutes>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return true;
        }

        try {
            double multiplier = Double.parseDouble(args[1]);
            int duration = Integer.parseInt(args[2].replace("m", ""));
            applyMultiplier(target, multiplier, duration);
            sender.sendMessage("Applied a " + multiplier + "x multiplier to " + target.getName() + " for " + duration + " minutes.");
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid number format.");
        }

        return true;
    }

    private void applyMultiplier(Player player, double multiplier, int duration) {
        UUID playerId = player.getUniqueId();
        // Cancel any existing removal task
        if (removalTasks.containsKey(playerId)) {
            removalTasks.get(playerId).cancel();
            removalTasks.remove(playerId);
        }

        // Apply the new multiplier
        multipliers.put(playerId, multiplier);

        // Schedule a new removal task
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                multipliers.remove(playerId);
                removalTasks.remove(playerId);
                player.sendMessage("Your selling multiplier has expired.");
            }
        }.runTaskLater(plugin, duration * 60 * 20); // Convert minutes to ticks

        // Store the task for potential cancellation
        removalTasks.put(playerId, task);

        player.sendMessage("Applied a " + multiplier + "x multiplier to you for " + duration + " minutes.");
    }

    public double getPlayerMultiplier(UUID playerUuid) {
        return multipliers.getOrDefault(playerUuid, 1.0); // Default to 1.0, meaning no multiplier
    }
}
