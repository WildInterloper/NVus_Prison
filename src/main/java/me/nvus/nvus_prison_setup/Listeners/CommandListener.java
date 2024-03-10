package me.nvus.nvus_prison_setup.Listeners;

import me.nvus.nvus_prison_setup.Configs.ConfigManager;
import me.nvus.nvus_prison_setup.Configs.SettingsMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandListener implements CommandExecutor {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;

    public CommandListener(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("nvus.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /nvus <reload|version|menu|autopickup|autoswitch> [arguments]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReloadCommand(sender);
                break;
            case "version":
                handleVersionCommand(sender);
                break;
            case "menu":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                    return true;
                }
                Player player = (Player) sender;
                new SettingsMenu(plugin, configManager).openSettingsMenu(player);
                break;
            case "autopickup":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /nvus autopickup <true|false>");
                    return true;
                }
                handleToggleConfigCommand(sender, "AutoPickup", args[1]);
                break;
            case "autoswitch":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /nvus autoswitch <true|false>");
                    return true;
                }
                handleToggleConfigCommand(sender, "AutoSwitch", args[1]);
                break;
            case "prisonerarmor": // New case for toggling PrisonerArmor
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /nvus prisonerarmor <true|false>");
                    return true;
                }
                handleToggleConfigCommand(sender, "PrisonerArmor", args[1]);
                break;

            case "restrictarmor": // New case for toggling RestrictArmor
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /nvus restrictarmor <true|false>");
                    return true;
                }
                handleToggleConfigCommand(sender, "RestrictArmor", args[1]);
                break;
            case "tooldamage": // New case for toggling RestrictArmor
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /nvus tooldamage <true|false>");
                    return true;
                }
                handleToggleConfigCommand(sender, "ToolDamage", args[1]);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Invalid command. Use /nvus for help.");
                return true;
        }

        return true;
    }

    private void handleReloadCommand(CommandSender sender) {
        configManager.reloadConfig("config.yml");
        configManager.reloadConfig("auto_switch.yml");
        configManager.reloadConfig("banned_items.yml");
        sender.sendMessage(ChatColor.GREEN + "Configuration files reloaded.");
    }

    private void handleVersionCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "Plugin version: " + plugin.getDescription().getVersion());
    }

    private void handleToggleConfigCommand(CommandSender sender, String key, String value) {
        boolean boolValue = Boolean.parseBoolean(value);
        FileConfiguration config = configManager.getConfig("config.yml");
        config.set(key, boolValue);
        configManager.saveConfig("config.yml");
        sender.sendMessage(ChatColor.GREEN + key + " set to " + boolValue + ".");
    }

}
