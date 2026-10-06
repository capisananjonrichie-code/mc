package dev.mtree.identity;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class IdentityPlugin extends JavaPlugin {
    private final Ranks ranks = new Ranks();
    private NametagManager nametags;
    private IntroManager intro;
    private LuckPermsHook luckPerms;   // null when LuckPerms is absent/disabled in config

    @Override
    public void onEnable() {
        saveDefaultConfig();
        nametags = new NametagManager(this, ranks);
        intro = new IntroManager(this);

        getServer().getPluginManager().registerEvents(new IdentityListener(this), this);
        MtreeCommand cmd = new MtreeCommand(this);
        getCommand("mtree").setExecutor(cmd);
        getCommand("mtree").setTabCompleter(cmd);

        setupLuckPerms();
        ranks.load(getConfig());
        nametags.rebuild();
    }

    @Override
    public void onDisable() {
        if (luckPerms != null) luckPerms.close();
        if (intro != null) intro.shutdown();
        if (nametags != null) nametags.shutdown();
    }

    private void setupLuckPerms() {
        if (luckPerms != null) { luckPerms.close(); luckPerms = null; }
        if (!getConfig().getBoolean("nametag.use-luckperms", true)) return;
        if (!Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            getLogger().info("LuckPerms not found - using default-rank for everyone.");
            return;
        }
        try {
            luckPerms = new LuckPermsHook(this, this::onRankData);
        } catch (Throwable t) {
            getLogger().warning("LuckPerms hook failed, using default rank: " + t.getMessage());
        }
    }

    /** LuckPerms events arrive off the main thread; hop back before touching Bukkit. */
    private void onRankData(UUID id) {
        Bukkit.getScheduler().runTask(this, () -> {
            var p = Bukkit.getPlayer(id);
            if (p != null) nametags.refreshIfChanged(p);
        });
    }

    void reloadAll() {
        reloadConfig();
        setupLuckPerms();
        ranks.load(getConfig());
        nametags.rebuild();
    }

    NametagManager nametags() { return nametags; }
    IntroManager intro() { return intro; }
    LuckPermsHook luckPerms() { return luckPerms; }
}
