package dev.mtree.identity;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Consumer;

/** Only ever loaded when LuckPerms is present (see IdentityPlugin), so the plugin works without it. */
final class LuckPermsHook {
    private final LuckPerms lp = LuckPermsProvider.get();
    private final EventSubscription<UserDataRecalculateEvent> sub;

    LuckPermsHook(IdentityPlugin plugin, Consumer<UUID> onChange) {
        // Fires only when a user's data changes (rank changes, login) - no polling.
        sub = lp.getEventBus().subscribe(plugin, UserDataRecalculateEvent.class,
                e -> onChange.accept(e.getUser().getUniqueId()));
    }

    String primaryGroup(Player p) {
        User u = lp.getUserManager().getUser(p.getUniqueId());
        return u == null ? null : u.getPrimaryGroup();
    }

    void close() { sub.close(); }
}
