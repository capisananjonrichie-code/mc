package dev.mtree.identity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Reads the ranks from config and works out which one a player has. */
final class Ranks {
    record Rank(String key, String prefix, String nameColor, String suffix, String tab) {}

    private final Map<String, Rank> byKey = new HashMap<>();
    private final Map<String, String> groupToKey = new HashMap<>();
    private final List<String> order = new ArrayList<>();
    private String defaultKey = "member";

    void load(FileConfiguration cfg) {
        byKey.clear(); groupToKey.clear(); order.clear();
        defaultKey = cfg.getString("default-rank", "member").toLowerCase();
        ConfigurationSection sec = cfg.getConfigurationSection("ranks");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                ConfigurationSection r = sec.getConfigurationSection(key);
                if (r == null) continue;
                String k = key.toLowerCase();
                byKey.put(k, new Rank(k, r.getString("prefix", ""), r.getString("name-color", "&f"),
                        r.getString("suffix", ""), r.getString("tab", null)));
                groupToKey.put(r.getString("luckperms-group", k).toLowerCase(), k);
                order.add(k);
            }
        }
        if (!byKey.containsKey(defaultKey)) {
            byKey.put(defaultKey, new Rank(defaultKey, "", "&f", "", null));
            order.add(defaultKey);
        }
    }

    List<String> keys() { return order; }
    Rank get(String key) { return byKey.getOrDefault(key, byKey.get(defaultKey)); }

    /** Rank key for a player; LuckPerms group first, default rank otherwise. */
    String keyFor(Player p, LuckPermsHook lp) {
        if (lp != null) {
            String group = lp.primaryGroup(p);
            if (group != null) {
                String k = groupToKey.get(group.toLowerCase());
                if (k != null) return k;
            }
        }
        return defaultKey;
    }
}
