package me.bramhaag.guilds.guild;

import com.google.gson.GsonBuilder;
import me.bramhaag.guilds.IHandler;
import me.bramhaag.guilds.Main;

import java.util.HashMap;
import java.util.logging.Level;

public class GuildHandler implements IHandler {

    private HashMap<String, Guild> guilds;

    @Override
    public void enable() {
        guilds = new HashMap<>();

        initialize();
    }

    @Override
    public void disable() {
        guilds.clear();
        guilds = null;
    }

    private void initialize() {
        Main.getInstance().getDatabaseProvider().getGuilds(((result, exception) -> {
            if(result == null && exception != null) {
                Main.getInstance().getLogger().log(Level.SEVERE, "An error occurred while loading guilds");
                exception.printStackTrace();
                return;
            }

            if(result != null) {
                guilds = result;
                System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(result));
            }
        }));

        guilds.values().forEach(this::addGuild);
    }

    public void addGuild(Guild guild) {
        guilds.put(guild.getName(), guild);
    }

    public void setGuilds(HashMap<String, Guild> guilds) {
        this.guilds = guilds;
    }

    public HashMap<String, Guild> getGuilds() {
        return guilds;
    }
}
