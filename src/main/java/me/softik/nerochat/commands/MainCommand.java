package me.softik.nerochat.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import me.softik.nerochat.NeroChat;
import me.softik.nerochat.utils.CommonTool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainCommand implements CommandExecutor, TabExecutor {
    private final NeroChat plugin;
    private FileConfiguration config;

    public MainCommand(NeroChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "help":
                    if (sender.hasPermission("nerochat.help")) {
                        ComponentBuilder builder = new ComponentBuilder("---" + CommonTool.getPrefix() + "---").color(ChatColor.GOLD);

                        plugin.getDescription().getCommands().forEach((name, info) ->
                                builder.append("\n/" + name)
                                        .color(ChatColor.GOLD)
                                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + name + " "))
                                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                new ComponentBuilder("Click me!")
                                                        .color(ChatColor.GOLD)
                                                        .create()
                                        ))
                                        .append(" - ")
                                        .color(ChatColor.GOLD)
                                        .append(info.get("description").toString()));

                        sender.spigot().sendMessage(builder.create());
                    } else {
                        sender.sendMessage(command.getPermissionMessage());
                    }

                    break;
                case "version":
                    if (sender.hasPermission("nerochat.version")) {
                        sender.sendMessage(ChatColor.GOLD + "Currently running: " + plugin.getDescription().getFullName());
                    } else {
                        sender.sendMessage(command.getPermissionMessage());
                    }

                    break;
                case "reload":
                    if (sender.hasPermission("nerochat.reload")) {
                        try {
                            plugin.getConfig().load(new File(plugin.getDataFolder(), "config.yml"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (InvalidConfigurationException e) {
                            throw new RuntimeException(e);
                        }
                        sender.sendMessage("Reloaded the config!");
                        List<String> regexList = plugin.getConfig().getStringList("RegexFilter.Chat.Allowed-Regex");
                        plugin.getLogger().info("The regex you added:");
                        for (String regex : regexList) {
                            plugin.getLogger().info("- " + regex);
                        }
                    } else {
                        sender.sendMessage(command.getPermissionMessage());
                    }

                    break;
                default:
                    return false;
            }
        } else {
            return false;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> possibleCommands = new ArrayList<>();
            List<String> completions = new ArrayList<>();

            if (sender.hasPermission("nerochat.help")) {
                possibleCommands.add("help");
            }

            if (sender.hasPermission("nerochat.reload")) {
                possibleCommands.add("reload");
            }

            StringUtil.copyPartialMatches(args[0], possibleCommands, completions);
            Collections.sort(completions);

            return completions;
        } else {
            return new ArrayList<>();
        }
    }
}
