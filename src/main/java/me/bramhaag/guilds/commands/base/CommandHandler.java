package me.bramhaag.guilds.commands.base;

import me.bramhaag.guilds.IHandler;
import me.bramhaag.guilds.message.Message;
import me.bramhaag.guilds.util.ConfirmAction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CommandHandler implements CommandExecutor, IHandler {

    private List<CommandBase> commands;
    private HashMap<Player, ConfirmAction> actions;

    @Override
    public void enable() {
        commands = new ArrayList<>();
        actions = new HashMap<>();
    }

    @Override
    public void disable() {
        commands.clear();
        commands = null;

        actions.clear();
        actions = null;
    }

    public void register(CommandBase command) {
        commands.add(command);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        if(!cmd.getName().equalsIgnoreCase("guild")) {
            return true;
        }

        if(args.length == 0 || args[0].isEmpty()) {
            getCommand("help").execute(sender, args);
            return true;
        }

        for(CommandBase command : commands) {
            if (!command.getName().equalsIgnoreCase(args[0]) && !command.getAliases().contains(args[0].toLowerCase())) {
                continue;
            }

            if(!command.allowConsole() && !(sender instanceof Player)) {
                Message.sendMessage(sender, Message.COMMAND_ERROR_CONSOLE);
                return true;
            }

            if(!sender.hasPermission(command.getPermission())) {
                Message.sendMessage(sender, Message.COMMAND_ERROR_PERMISSION);
                return true;
            }

            args = Arrays.copyOfRange(args, 1, args.length);

            if((command.getMinimumArguments() != -1 && command.getMinimumArguments() > args.length) || (command.getMaximumArguments() != -1 && command.getMaximumArguments() < args.length)) {
                Message.sendMessage(sender, Message.COMMAND_ERROR_ARGS);
                return true;
            }

            if (command.allowConsole()) {
                command.execute(sender, args);
                return true;
            }
            else {
                command.execute((Player) sender, args);
                return true;
            }
        }

        Message.sendMessage(sender, Message.COMMAND_ERROR_NOT_FOUND);
        return true;
    }

    private CommandBase getCommand(String name) {
        return commands.stream().filter(command -> command.getName() != null && command.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<CommandBase> getCommands() {
        return commands;
    }

    public HashMap<Player, ConfirmAction> getActions() {
        return actions;
    }

    public ConfirmAction addAction(Player player, ConfirmAction action) {
        actions.put(player, action);

        return action;
    }

    public void removeAction(Player player) {
        actions.remove(player);
    }
}
