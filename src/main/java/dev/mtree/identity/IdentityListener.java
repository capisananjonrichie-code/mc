package dev.mtree.identity;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

final class IdentityListener implements Listener {
    private final IdentityPlugin plugin;

    IdentityListener(IdentityPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.nametags().refresh(e.getPlayer());
        if (plugin.getConfig().getBoolean("intro.enabled", true)) {
            plugin.intro().play(e.getPlayer(), plugin.getConfig().getInt("intro.start-delay-ticks", 30));
        }
    }

    /** HIGHEST so we win over Essentials; only touches the message when enabled. */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoinMessage(PlayerJoinEvent e) {
        if (!plugin.getConfig().getBoolean("join-message.enabled", false)) return;
        String m = plugin.getConfig().getString("join-message.message", "");
        e.joinMessage(m.isBlank() ? null : Text.color(m.replace("%player%", e.getPlayer().getName())));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.nametags().remove(e.getPlayer());
        plugin.intro().cancel(e.getPlayer().getUniqueId());
    }
}
