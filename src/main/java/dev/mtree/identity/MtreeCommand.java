package dev.mtree.identity;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

final class MtreeCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUBS = List.of("reload", "nametag", "intro", "preview");
    private final IdentityPlugin plugin;

    MtreeCommand(IdentityPlugin plugin) { this.plugin = plugin; }

    private void msg(CommandSender s, String t) { s.sendMessage(Text.color("&8[&6MTREE&8] &7" + t)); }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] a) {
        if (!s.hasPermission("mtree.admin")) { msg(s, "&cYou do not have permission."); return true; }
        if (a.length == 0) { msg(s, "/mtree <reload|nametag <player>|intro <player>|preview>"); return true; }

        switch (a[0].toLowerCase()) {
            case "reload" -> { plugin.reloadAll(); msg(s, "&aReloaded."); }
            case "preview" -> {
                if (s instanceof Player p) plugin.intro().play(p, 0);
                else msg(s, "Players only.");
            }
            case "intro" -> {
                Player t = target(s, a);
                if (t != null) { plugin.intro().play(t, 0); msg(s, "Playing intro for " + t.getName() + "."); }
            }
            case "nametag" -> {
                Player t = target(s, a);
                if (t != null) {
                    plugin.nametags().refresh(t);
                    msg(s, "Refreshed " + t.getName() + " - rank: &f" + plugin.nametags().rankOf(t));
                }
            }
            default -> msg(s, "/mtree <reload|nametag <player>|intro <player>|preview>");
        }
        return true;
    }

    private Player target(CommandSender s, String[] a) {
        if (a.length < 2) { msg(s, "Specify a player."); return null; }
        Player t = Bukkit.getPlayerExact(a[1]);
        if (t == null) msg(s, "&cThat player is not online.");
        return t;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String l, String[] a) {
        List<String> out = new ArrayList<>();
        if (a.length == 1) {
            for (String x : SUBS) if (x.startsWith(a[0].toLowerCase())) out.add(x);
        } else if (a.length == 2 && (a[0].equalsIgnoreCase("nametag") || a[0].equalsIgnoreCase("intro"))) {
            for (Player p : Bukkit.getOnlinePlayers()) if (p.getName().toLowerCase().startsWith(a[1].toLowerCase())) out.add(p.getName());
        }
        return out;
    }
}
