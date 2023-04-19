package me.softik.nerochat.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import me.softik.nerochat.NeroChat;
import me.softik.nerochat.api.NeroWhisperEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import me.softik.nerochat.utils.ConfigCache;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonTool {

    public static Optional<Player> getPlayer(String name) {
        return Optional.ofNullable(Bukkit.getPlayer(name));
    }

    public static void sendWhisperTo(CommandSender sender, String message, CommandSender receiver) {
        if (sender == receiver) {
            sender.sendMessage("pmself");
            return;
        }

        if (NeroChat.getConfiguration().getBoolean("ReadableFormatting.Whisper", false)) {
            String trimmedMessage = message.trim();
            char lastChar = trimmedMessage.charAt(trimmedMessage.length() - 1);
            if (lastChar == '.' || lastChar == '!' || lastChar == '?') {
                char punctuationChar = '.';
                if (lastChar == '.' || lastChar == '!' || lastChar == '?') {
                    punctuationChar = lastChar;
                }
                message = StringUtils.capitalize(trimmedMessage.toLowerCase().replaceFirst("[" + ".!?" + "]+$", "")) + punctuationChar;
            } else {
                message = StringUtils.capitalize(trimmedMessage.toLowerCase()) + ".";
            }
        }

        if (!sender.hasPermission("nerochat.bypass")) {
            if (!NeroChat.getPlugin(NeroChat.class).getTempDataTool().isWhisperingEnabled(receiver)) {
                if (NeroChat.getPlugin(NeroChat.class).getConfig().getBoolean("onlyhidepms")) {
                    sendSender(sender, message, receiver);
                } else {
                    sender.sendMessage(CommonTool.getPrefix() + "This person has whispering disabled!");
                }
                return;
            }

            if (receiver instanceof Player && isVanished((Player) receiver)) {
                sender.sendMessage("notonline");
                return;
            }
        }

        NeroWhisperEvent neroWhisperEvent = new NeroWhisperEvent(sender, receiver, message);

        Bukkit.getPluginManager().callEvent(neroWhisperEvent);

        if (neroWhisperEvent.isCancelled())
            return;

        message = neroWhisperEvent.getMessage();

        List<String> regexList = NeroChat.getPlugin(NeroChat.class).getConfig().getStringList("RegexFilter.Whisper.Allowed-Regex");
        try {
            boolean useCaseInsensitive = NeroChat.getPlugin(NeroChat.class).getConfig().getBoolean("RegexFilter.Whisper.CaseInsensitive", true);
            for (String regex : regexList) {
                Pattern pattern;
                if (useCaseInsensitive) {
                    pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                } else {
                    pattern = Pattern.compile(regex);
                }
                Matcher matcher = pattern.matcher(message);
                if (matcher.find()) {
                    // The message contains an illegal pattern, so cancel the event
                    if (!NeroChat.getPlugin(NeroChat.class).getConfig().getBoolean("RegexFilter.Whisper.SilentMode", true) && NeroChat.getPlugin(NeroChat.class).getConfig().getBoolean("RegexFilter.Whisper.PlayerNotify", true)) {
                        sender.sendMessage("PlayerNotify");
                    }
                    if (NeroChat.getPlugin(NeroChat.class).getConfig().getBoolean("RegexFilter.Whisper.ConsoleNotify", true)) {
                        NeroChat.getPlugin(NeroChat.class).getLogger().warning(sender.getName() + " tried to send a whisper that didn't match the regex: " + message);
                    }
                    if (NeroChat.getPlugin(NeroChat.class).getConfig().getBoolean("RegexFilter.Whisper.SilentMode", false)) {
                        sendSender(sender, message, receiver);
                    }
                    return;
                }
            }
            sendSender(sender, message, receiver);
            sendReceiver(sender, message, receiver);
            NeroChat.getPlugin(NeroChat.class).getCacheTool().sendMessage(sender, receiver);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void sendSender(CommandSender sender, String message, CommandSender receiver) {
        String senderString = ChatColor.translateAlternateColorCodes('&', NeroChat.getConfiguration().getString("Whisper.to", "&dYou whisper to %player%&d: %message%")
                .replace("%player%", ChatColor.stripColor(new UniqueSender(receiver).getDisplayName()))
                .replace("%message%", message));

        sender.spigot().sendMessage(new TextComponent(TextComponent.fromLegacyText(senderString)));
    }

    private static void sendReceiver(CommandSender sender, String message, CommandSender receiver) {
        String receiverString = ChatColor.translateAlternateColorCodes('&', NeroChat.getConfiguration().getString("Whisper.from", "&d%player%&d whispers: %message%")
                .replace("%player%", ChatColor.stripColor(new UniqueSender(sender).getDisplayName()))
                .replace("%message%", message));

        receiver.spigot().sendMessage(new TextComponent(TextComponent.fromLegacyText(receiverString)));
    }

    public static String mergeArgs(String[] args, int start) {
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }

    public static String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', NeroChat.getConfiguration().getString("prefix", "GREEN: '>'"));
    }

    public static ChatColor getChatColorFor(String message, Player player) {
        for (String str : NeroChat.getConfiguration().getConfigurationSection("Prefixes").getKeys(false)) {
            if (!NeroChat.getConfiguration().getString("Prefixes." + str, "GREEN: '>'").equalsIgnoreCase("/") && message.toLowerCase().startsWith(NeroChat.getConfiguration().getString("Prefixes." + str, "GREEN: '>'"))) {
                if (player.hasPermission("nerochat." + str)) {
                    return ChatColor.valueOf(str);
                } else {
                    return ChatColor.WHITE;
                }
            }
        }

        return ChatColor.WHITE;
    }

    public static String getFormat(CommandSender sender) {
        String str = ChatColor.translateAlternateColorCodes('&', NeroChat.getConfiguration().getString("Main.chat-format", "<%player%&r>").replace("%player%", getName(sender)));

        if (sender instanceof Player && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            str = parse((OfflinePlayer) sender, str);
        }

        return str;
    }

    public static void sendChatMessage(Player chatter, String message, Player receiver) {
        ComponentBuilder builder = new ComponentBuilder(CommonTool.getFormat(chatter));

        if (receiver.hasPermission("nerochat.playernamereply")) {
            builder.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/w " + ChatColor.stripColor(chatter.getDisplayName()) + " "));

            String hoverText = NeroChat.getConfiguration().getString("Main.hover-text", "&6Message &3%player%");

            builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(
                            ChatColor.translateAlternateColorCodes('&',
                                    hoverText.replace("%player%",
                                            ChatColor.stripColor(chatter.getDisplayName())
                                    )
                            )
                    ).create()
            ));
        }

        builder.append(" ").reset();

        builder.append(new TextComponent(TextComponent.fromLegacyText(message)));

        builder.color(CommonTool.getChatColorFor(message, chatter));

        receiver.spigot().sendMessage(builder.create());
    }

    private static String getName(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (NeroChat.getConfiguration().getBoolean("Main.display-nickname-color", true)) {
                return ChatColor.stripColor(player.getDisplayName());
            } else {
                return player.getDisplayName();
            }
        } else if (sender instanceof ConsoleCommandSender) {
            return ChatColor.translateAlternateColorCodes('&', NeroChat.getConfiguration().getString("Main.console-name", "[console]"));
        } else {
            return sender.getName();
        }
    }

    public static String parse(OfflinePlayer player, String str) {
        return PlaceholderAPI.setPlaceholders(player, str);
    }

    private static boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }
}
