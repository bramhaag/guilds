package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.guild.GuildMember;
import me.bramhaag.guilds.guild.GuildRole;
import me.bramhaag.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandAccept extends CommandBase {

    public CommandAccept() {
        super("accept", "Accept an invite to a guild", "guilds.command.accept", false, null, new String[] { "<guild id>" }, 1, 1);
    }

    public void execute(Player player, String[] args) {
        if(Guild.getGuild(player.getUniqueId()) != null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ALREADY_IN_GUILD);
            return;
        }

        Guild guild = Guild.getGuild(args[0]);
        if(guild == null) {
            Message.sendMessage(player, Message.COMMAND_ACCEPT_GUILD_NOT_FOUND);
            return;
        }

        if(!guild.getInvitedMembers().contains(player.getUniqueId())) {
            Message.sendMessage(player, Message.COMMAND_ACCEPT_NOT_INVITED);
            return;
        }

        int maxMembers = Main.getInstance().getConfig().getInt("members.max-members");
        if(maxMembers != -1 && guild.getMembers().size() >= maxMembers) {
            Message.sendMessage(player, Message.COMMAND_ACCEPT_GUILD_FULL);
            return;
        }

        guild.addMember(player.getUniqueId(), GuildRole.MEMBER);
        guild.removeInvitedPlayer(player.getUniqueId());

        Message.sendMessage(player, Message.COMMAND_ACCEPT_SUCCESSFUL);

        for(GuildMember member : guild.getMembers()) {
            Player receiver = Bukkit.getPlayer(member.getUniqueId());
            if (receiver == null || !receiver.isOnline()) {
                continue;
            }
            Message.sendMessage(player, Message.COMMAND_ACCEPT_PLAYER_JOINED.replace("{player}", player.getName()));
        }
    }
}
