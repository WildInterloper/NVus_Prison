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

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

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
            StringBuilder message = new StringBuilder();
            message.append(ChatColor.GREEN).append("\n");
            message.append(ChatColor.GOLD).append("NVus Prison GOLD Edition:\n");
            message.append(ChatColor.DARK_GRAY).append("==========================\n");
            message.append(ChatColor.GREEN).append("/nvus reload - Reloads all configuration files.\n");
            message.append(ChatColor.GREEN).append("/nvus version - Shows the plugin version.\n");
            message.append(ChatColor.GREEN).append("/nvus menu - Open a GUI menu to toggle options on/off.\n");
            message.append(ChatColor.GREEN).append("/nvus autopickup true|false - Toggle prisoner auto pickup of mined resources.\n");
            message.append(ChatColor.GREEN).append("/nvus autoswitch true|false - Toggle prisoner auto switching to correct tools when mining.\n");
            message.append(ChatColor.GREEN).append("/nvus prisonerarmor true|false - Toggle if prisoners spawn with orange leather armor aka jumpsuits.\n");
            message.append(ChatColor.GREEN).append("/nvus restrictarmor true|false - Toggle if prisoners can change their armor or not.\n");
            message.append(ChatColor.GREEN).append("/nvus tooldamage true|false - Toggle if prisoner tools receive damage. FALSE = no damage!\n");
            message.append(ChatColor.AQUA).append("Support: https://FNGnation.net/discord\n");
            message.append(ChatColor.AQUA).append("\n");
            message.append(ChatColor.LIGHT_PURPLE).append("GANGS:\n");
            message.append(ChatColor.DARK_GRAY).append("=======\n");
            message.append(ChatColor.GREEN).append("/gang - Use this to see full gang command list!");
            message.append(ChatColor.GREEN).append("\n");

            sender.sendMessage(message.toString());


            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReloadCommand(sender);
                break;
            case "version":
                handleVersionCommand(sender);
                break;
            case "id":
                handleIdCommand(sender);
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
            case "treefarm": // New case for toggling RestrictArmor
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /nvus treefarm <true|false>");
                    return true;
                }
                handleToggleConfigCommand(sender, "TreeFarm", args[1]);
                break;
            case "autosell": // New case for toggling RestrictArmor
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /nvus autosell <true|false>");
                    return true;
                }
                handleToggleConfigCommand(sender, "AutoSell", args[1]);
                break;
            case "sellall": // New case for toggling RestrictArmor
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /nvus sellall <true|false>");
                    return true;
                }
                handleToggleConfigCommand(sender, "SellAll", args[1]);
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
        configManager.reloadConfig("item_prices.yml");
        sender.sendMessage(ChatColor.GREEN + "Configuration files reloaded.");
    }

    private void handleVersionCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "Plugin version: " + plugin.getDescription().getVersion());
    }

    public void handleIdCommand(CommandSender sender) {
        // Check if the sender is a player
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Your existing logic here
            // For example, sending the player their username and UUID in a simple message
            player.sendMessage("Your username: " + player.getName());
            player.sendMessage("Your UUID: " + player.getUniqueId().toString());

            // Now send the clickable UUID message
            sendClickableUUID(player);
        } else {
            // If the command sender is not a player (e.g., console), handle appropriately
            sender.sendMessage("This command can only be used by a player.");
        }
    }

    public void sendClickableUUID(Player player) {
        String uuid = player.getUniqueId().toString();

        TextComponent message = new TextComponent("Click here to copy your UUID");
        message.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, uuid));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to copy your UUID").color(net.md_5.bungee.api.ChatColor.YELLOW).create()));

        player.spigot().sendMessage(message);
    }


    private void handleToggleConfigCommand(CommandSender sender, String key, String value) {
        boolean boolValue = Boolean.parseBoolean(value);
        FileConfiguration config = configManager.getConfig("config.yml");
        config.set(key, boolValue);
        configManager.saveConfig("config.yml");
        sender.sendMessage(ChatColor.GREEN + key + " set to " + boolValue + ".");
    }


}
