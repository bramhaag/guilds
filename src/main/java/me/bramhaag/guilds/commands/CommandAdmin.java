package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.guild.GuildRole;
import me.bramhaag.guilds.message.Message;
import me.bramhaag.guilds.util.ConfirmAction;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class CommandAdmin extends CommandBase {

    public CommandAdmin() {
        super("admin", "Admin command for managing guilds", "guilds.commands.admin", true, null, new String[] { "<remove | info> <guild name>" }, 2, 2);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Guild guild = Guild.getGuild(args[1]);

        if(guild == null) {
            Message.sendMessage(sender, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        if(args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove")) {
            Message.sendMessage(sender, Message.COMMAND_ADMIN_DELETE_WARNING.replace("{guild}", args[1]));

            Main.getInstance().getCommandHandler().addAction(sender, new ConfirmAction() {
                @Override
                public void accept() {
                    Main.getInstance().getDatabaseProvider().removeGuild(guild, ((result, exception) -> {
                        if(result) {
                            Message.sendMessage(sender, Message.COMMAND_ADMIN_DELETE_SUCCESSFUL.replace("{guild}", guild.getName()));
                            Main.getInstance().getScoreboardHandler().update();
                        }
                        else {
                            Message.sendMessage(sender, Message.COMMAND_ADMIN_DELETE_ERROR);

                            Main.getInstance().getLogger().log(Level.SEVERE, String.format("An error occurred while player '%s' was trying to delete guild '%s'", sender.getName(), guild.getName()));
                            if(exception != null) {
                                exception.printStackTrace();
                            }
                        }
                    }));

                    Main.getInstance().getCommandHandler().removeAction(sender);
                }

                @Override
                public void decline() {
                    Message.sendMessage(sender, Message.COMMAND_ADMIN_DELETE_CANCELLED);
                    Main.getInstance().getCommandHandler().removeAction(sender);
                }
            });
        }
        else if(args[0].equalsIgnoreCase("info")) {
            Message.sendMessage(sender, Message.COMMAND_INFO_HEADER.replace("{guild}", guild.getName()));
            Message.sendMessage(sender, Message.COMMAND_INFO_NAME.replace("{guild}", guild.getName(), "{prefix}", guild.getPrefix()));
            Message.sendMessage(sender, Message.COMMAND_INFO_MASTER.replace("{master}", Bukkit.getPlayer(guild.getGuildMaster().getUniqueId()).getName()));
            Message.sendMessage(sender, Message.COMMAND_INFO_MEMBER_COUNT.replace("{members}", String.valueOf(guild.getMembers().size())));
        }
    }
}