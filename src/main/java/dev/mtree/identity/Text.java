package dev.mtree.identity;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

final class Text {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private Text() {}

    static Component color(String s) { return LEGACY.deserialize(s == null ? "" : s); }

    /** "&f" -> WHITE. Returns null if the string is not a plain named colour code. */
    static NamedTextColor named(String code) {
        if (code == null || code.isBlank()) return null;
        TextColor c = LEGACY.deserialize(code + "x").color();
        return c instanceof NamedTextColor n ? n : null;
    }
}
