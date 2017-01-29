package me.bramhaag.guilds.leaderboard;

import me.bramhaag.guilds.IHandler;
import me.bramhaag.guilds.Main;
import org.spigotmc.SneakyThrow;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class LeaderboardHandler implements IHandler {

    private List<Leaderboard> leaderboards;

    @Override
    public void enable() {
        leaderboards = new ArrayList<>();

        initialize();
    }

    @Override
    public void disable() {
        leaderboards.clear();
        leaderboards = null;
    }

    private void initialize() {
        Main.getInstance().getDatabaseProvider().getLeaderboards((result, exception) -> {
            if (result == null && exception != null) {
                Main.getInstance().getLogger().log(Level.SEVERE, "An error occurred while loading leaderboards");
                exception.printStackTrace();
                return;
            }

            if (result != null) {
                leaderboards = result;
                Main.getInstance().getScoreboardHandler().enable();
            }
        });
    }

    public void addLeaderboard(Leaderboard leaderboard) {
        leaderboards.add(leaderboard);

        Main.getInstance().getDatabaseProvider().createLeaderboard(leaderboard, (result, exception) -> {
            if(result == null && exception != null) {
                SneakyThrow.sneaky(exception);
            }
        });
    }

    public void removeLeaderboard(Leaderboard leaderboard) {
        leaderboards.remove(leaderboard);

        Main.getInstance().getDatabaseProvider().removeLeaderboard(leaderboard, (result, exception) -> {
            if(!result && exception != null) {
                SneakyThrow.sneaky(exception);
            }
        });
    }

    public List<Leaderboard> getLeaderboards() {
        return leaderboards;
    }

    public Leaderboard getLeaderboard(String name, Leaderboard.LeaderboardType leaderboardType) {
        return leaderboards.stream().filter(leaderboard -> leaderboard.getName().equals(name) && leaderboard.getLeaderboardType() == leaderboardType).findFirst().orElse(null);
    }
}
