package me.softik.nerochat.commands;

import me.softik.nerochat.NeroChat;
import me.softik.nerochat.commands.ignore.HardIgnoreCommand;
import me.softik.nerochat.commands.ignore.IgnoreListCommand;
import me.softik.nerochat.commands.nerochat.NeroChatCmd;
import me.softik.nerochat.commands.toggle.ToggleChatCommand;
import me.softik.nerochat.commands.toggle.ToggleWhisperingCommand;
import me.softik.nerochat.commands.whisper.LastCommand;
import me.softik.nerochat.commands.whisper.ReplyCommand;
import me.softik.nerochat.commands.whisper.WhisperCommand;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface NeroChatCommand extends CommandExecutor, TabCompleter {

    String label();

    List<String> NO_COMPLETIONS = Collections.emptyList();
    Set<NeroChatCommand> commands = new HashSet<>();

    static void reloadCommands() {
        commands.clear();
        NeroChat plugin = NeroChat.getInstance();

        // nerochat
        commands.add(new NeroChatCmd());
        // ignore
        commands.add(new HardIgnoreCommand());
        commands.add(new IgnoreListCommand());
        // toggle
        commands.add(new ToggleChatCommand());
        commands.add(new ToggleWhisperingCommand());
        // whisper
        commands.add(new LastCommand());
        commands.add(new ReplyCommand());
        commands.add(new WhisperCommand());

        CommandMap commandMap = plugin.getServer().getCommandMap();

        for (NeroChatCommand command : commands) {
            plugin.getCommand(command.label()).unregister(commandMap);
            plugin.getCommand(command.label()).setExecutor(command);
            plugin.getCommand(command.label()).setTabCompleter(command);
        }
    }
}
