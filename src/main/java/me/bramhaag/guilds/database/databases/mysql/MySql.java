package me.bramhaag.guilds.database.databases.mysql;

import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import com.sun.rowset.CachedRowSetImpl;
import com.zaxxer.hikari.HikariDataSource;
import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.database.Callback;
import me.bramhaag.guilds.database.DatabaseProvider;

import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.guild.GuildMember;
import me.bramhaag.guilds.guild.GuildRole;
import org.bukkit.configuration.ConfigurationSection;

import javax.sql.rowset.CachedRowSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;


public class MySql extends DatabaseProvider {
    private HikariDataSource hikari;

    @Override
    public void initialize() {
        ConfigurationSection databaseSection = Main.getInstance().getConfig().getConfigurationSection("database");
        if(databaseSection == null) {
            //TODO probably should disable the plugin
            throw new IllegalStateException("MySQL database configured incorrectly, cannot continue properly");
        }

        hikari = new HikariDataSource();
        hikari.setMaximumPoolSize(databaseSection.getInt("pool-size"));

        hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hikari.addDataSourceProperty("serverName", databaseSection.getString("host"));
        hikari.addDataSourceProperty("port", databaseSection.getInt("port"));
        hikari.addDataSourceProperty("databaseName", databaseSection.getString("database"));

        hikari.addDataSourceProperty("user", databaseSection.getString("username"));
        hikari.addDataSourceProperty("password", databaseSection.getString("password"));

        hikari.validate();

        Main.newChain()
            .async(() -> execute(Query.CREATE_TABLE_GUILDS))
            .async(() -> execute(Query.CREATE_TABLE_MEMBERS))
            .async(() -> execute(Query.CREATE_TABLE_INVITED_MEMBERS))
            .sync(() -> Main.getInstance().getLogger().log(Level.INFO, "Tables 'guilds', 'members' and 'invited_members' created!"))
        .execute();
    }

    @Override
    public void createGuild(Guild guild, Callback<Boolean, Exception> callback) {
        TaskChain<?> chain = Main.newChain();

        chain
            .async(() -> {
                boolean result = execute(Query.CREATE_GUILD, guild.getName());
                chain.setTaskData("create_guild", result);
            })
            .async(() -> {
                boolean result = execute(Query.ADD_MEMBER, guild.getGuildMaster().getUniqueId().toString(), guild.getName(), GuildRole.MASTER.getLevel());
                chain.setTaskData("add_member", result);
            })
            .sync(() -> {
                boolean createGuildResult = chain.getTaskData("create_guild");
                boolean addMemberResult = chain.getTaskData("add_member");

                callback.call(createGuildResult && addMemberResult, null);
            })
        .execute();
    }

    @Override
    public void removeGuild(Guild guild, Callback<Boolean, Exception> callback) {
        TaskChain<?> chain = Main.newChain();

        chain.
            async(() -> {
                boolean result = true;

                for(GuildMember member : guild.getMembers()) {
                    if(!execute(Query.REMOVE_MEMBER, member.getUniqueId())) {
                        result = false;
                    }
                }

                chain.setTaskData("remove_member", result);
            })
            .async(() -> {
                boolean result = execute(Query.REMOVE_GUILD, guild.getName());
                chain.setTaskData("remove_guild", result);
            })
            .sync(() -> {
                boolean removeMemberResult = chain.getTaskData("remove_member");
                boolean removeGuildResult = chain.getTaskData("remove_guild");

                callback.call(removeMemberResult && removeGuildResult, null);
            });
    }

    @Override
    public void getGuilds(Callback<HashMap<String, Guild>, Exception> callback) {

    }

    @Override
    public void updateGuild(Guild guild, Callback<Boolean, Exception> callback) {

    }

    private boolean execute(String query, Object... parameters) {

        Connection connection;
        PreparedStatement statement;

        try {
            connection = hikari.getConnection();
            statement = connection.prepareStatement(query);

            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }

            statement.execute();
            return true;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ResultSet executeQuery(String query, Object... parameters) {
        Connection connection;
        PreparedStatement statement;

        try {
            connection = hikari.getConnection();
            statement = connection.prepareStatement(query);

            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }

            CachedRowSet resultCached = new CachedRowSetImpl();
            ResultSet resultSet = statement.executeQuery();

            resultCached.populate(resultSet);
            resultSet.close();

            return resultCached;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
