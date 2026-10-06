package dev.mtree.identity;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Nametags use vanilla scoreboard teams (prefix/suffix/colour) - no entities, no packets.
 * One team per rank ("mt_0", "mt_1", ...). Updated only on join/quit/rank change/reload.
 */
final class NametagManager {
    private static final String TEAM_PREFIX = "mt_";
    private static final String HEALTH_OBJ = "mt_health";

    private final IdentityPlugin plugin;
    private final Ranks ranks;
    private final Map<UUID, String> lastRank = new HashMap<>();
    private final Map<String, String> teamNameByRank = new HashMap<>();

    NametagManager(IdentityPlugin plugin, Ranks ranks) {
        this.plugin = plugin;
        this.ranks = ranks;
    }

    private Scoreboard board() { return Bukkit.getScoreboardManager().getMainScoreboard(); }
    private FileConfiguration cfg() { return plugin.getConfig(); }

    /** (Re)creates teams from config and refreshes everyone online. */
    void rebuild() {
        clearTeams();
        teamNameByRank.clear();
        lastRank.clear();

        if (cfg().getBoolean("nametag.enabled", true)) {
            boolean showRank = cfg().getBoolean("nametag.show-rank", true);
            int i = 0;
            for (String key : ranks.keys()) {
                Ranks.Rank r = ranks.get(key);
                Team t = board().registerNewTeam(TEAM_PREFIX + i);
                teamNameByRank.put(key, t.getName());
                if (showRank) {
                    t.prefix(Text.color(r.prefix()));
                    t.suffix(Text.color(r.suffix()));
                }
                NamedTextColor c = Text.named(r.nameColor());
                if (c != null) t.color(c);
                i++;
            }
        }
        updateHealthObjective();
        for (Player p : Bukkit.getOnlinePlayers()) refresh(p);
    }

    private void updateHealthObjective() {
        Objective old = board().getObjective(HEALTH_OBJ);
        if (old != null) old.unregister();
        if (cfg().getBoolean("nametag.enabled", true) && cfg().getBoolean("nametag.show-health", false)) {
            Objective o = board().registerNewObjective(HEALTH_OBJ, Criteria.HEALTH, Component.text("\u2764", NamedTextColor.RED));
            o.setDisplaySlot(DisplaySlot.BELOW_NAME);
        }
    }

    /** Apply nametag + tab name for one player. */
    void refresh(Player p) {
        String key = ranks.keyFor(p, plugin.luckPerms());
        lastRank.put(p.getUniqueId(), key);

        String teamName = teamNameByRank.get(key);
        if (teamName != null) {
            Team t = board().getTeam(teamName);
            if (t != null && !t.hasEntry(p.getName())) t.addEntry(p.getName());
        }
        refreshTab(p, key);
    }

    /** Called from the LuckPerms hook; does nothing unless the rank actually changed. */
    void refreshIfChanged(Player p) {
        String key = ranks.keyFor(p, plugin.luckPerms());
        if (!key.equals(lastRank.get(p.getUniqueId()))) refresh(p);
    }

    private void refreshTab(Player p, String key) {
        if (!cfg().getBoolean("tab.enabled", true)) { p.playerListName(null); return; }
        Ranks.Rank r = ranks.get(key);
        String format = r.tab() != null ? r.tab() : cfg().getString("tab.format", "{prefix}{name-color}{name}{suffix}");
        String s = format.replace("{prefix}", r.prefix()).replace("{name-color}", r.nameColor())
                .replace("{suffix}", r.suffix()).replace("{rank}", r.key()).replace("{name}", p.getName());
        p.playerListName(Text.color(s));
    }

    String rankOf(Player p) { return lastRank.getOrDefault(p.getUniqueId(), ranks.keyFor(p, plugin.luckPerms())); }

    void remove(Player p) {
        lastRank.remove(p.getUniqueId());
        Team t = board().getEntryTeam(p.getName());
        if (t != null && t.getName().startsWith(TEAM_PREFIX)) t.removeEntry(p.getName());
    }

    private void clearTeams() {
        for (Team t : board().getTeams()) if (t.getName().startsWith(TEAM_PREFIX)) t.unregister();
    }

    void shutdown() {
        clearTeams();
        Objective o = board().getObjective(HEALTH_OBJ);
        if (o != null) o.unregister();
        for (Player p : Bukkit.getOnlinePlayers()) p.playerListName(null);
        lastRank.clear();
    }
}
