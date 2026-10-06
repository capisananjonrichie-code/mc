package dev.mtree.identity;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 4 one-shot scheduled tasks per intro (no loop, no per-tick work), using vanilla titles + sound.
 * Titles/subtitles are supported by Geyser, so Bedrock players see the same thing.
 * Stage start (ticks) at the 6.5 s reference length: 0 / 30 / 60 / 100, end 130.
 */
final class IntroManager {
    private static final int[] START = {0, 30, 60, 100};
    private static final int END = 130;

    private final IdentityPlugin plugin;
    private final Map<UUID, List<BukkitTask>> running = new HashMap<>();

    IntroManager(IdentityPlugin plugin) { this.plugin = plugin; }

    void play(Player p, int delayTicks) {
        cancel(p.getUniqueId());
        FileConfiguration c = plugin.getConfig();
        double seconds = Math.max(3.0, Math.min(12.0, c.getDouble("intro.duration-seconds", 6.5)));
        double scale = seconds * 20.0 / END;
        boolean sound = c.getBoolean("intro.sound", true);

        Component[][] stages = {
                {Text.color(c.getString("intro.title")), Component.empty()},
                {Component.text(" "), Text.color(c.getString("intro.subtitle"))},
                {Component.text(" "), Text.color(c.getString("intro.message"))},
                {Component.text(" "), Text.color(c.getString("intro.player-message", "").replace("%player%", p.getName()))}
        };

        List<BukkitTask> tasks = new ArrayList<>();
        UUID id = p.getUniqueId();
        for (int i = 0; i < stages.length; i++) {
            final int idx = i;
            int start = (int) Math.round(START[i] * scale);
            int len = (int) Math.round(((i + 1 < START.length ? START[i + 1] : END) - START[i]) * scale);
            int fadeIn = Math.min(6, len / 3);
            int fadeOut = (i == stages.length - 1) ? Math.min(12, len / 2) : Math.min(5, len / 3);
            Title.Times times = Title.Times.times(ticks(fadeIn), ticks(Math.max(1, len - fadeIn - fadeOut)), ticks(fadeOut));

            tasks.add(Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Player online = Bukkit.getPlayer(id);
                if (online == null) { cancel(id); return; }
                online.showTitle(Title.title(stages[idx][0], stages[idx][1], times));
                if (sound && (idx == 0 || idx == 3)) {
                    online.playSound(online.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.4f, idx == 0 ? 0.8f : 1.2f);
                }
                if (idx == stages.length - 1) running.remove(id);   // finished: release everything
            }, delayTicks + start));
        }
        running.put(id, tasks);
    }

    private static Duration ticks(int t) { return Duration.ofMillis(t * 50L); }

    void cancel(UUID id) {
        List<BukkitTask> t = running.remove(id);
        if (t != null) t.forEach(BukkitTask::cancel);
    }

    void shutdown() {
        running.values().forEach(l -> l.forEach(BukkitTask::cancel));
        running.clear();
    }
}
