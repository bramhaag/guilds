package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandInfo extends CommandBase {

    public CommandInfo() {
        super("info", "View your guild's info", "guilds.commands.info", false, null, null, 0, 0);
    }

    @Override
    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());

        if(guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        Message.sendMessage(player, Message.COMMAND_INFO_HEADER.replace("{guild}", guild.getName()));
        Message.sendMessage(player, Message.COMMAND_INFO_NAME.replace("{guild}", guild.getName()));
        Message.sendMessage(player, Message.COMMAND_INFO_MASTER.replace("{master}", Bukkit.getPlayer(guild.getGuildMaster().getUuid()).getName()));
        Message.sendMessage(player, Message.COMMAND_INFO_MEMBER_COUNT.replace("{members}", String.valueOf(guild.getMembers().size())));
        Message.sendMessage(player, Message.COMMAND_INFO_RANK.replace("{rank}", guild.getMember(player.getUniqueId()).getRole().name()));
    }
}