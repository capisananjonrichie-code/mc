# MTREEPlayerIdentity (Paper 1.21.11, Java 21)

LuckPerms-driven nametags, TAB-list names, optional join message and a ~6.5 s welcome intro.
Independent of MTREEDeathPenalty. LuckPerms is optional.

## Build
Requires JDK 21 and Maven.
    mvn clean package
Output: `target/MTREEPlayerIdentity.jar`

## Install
Drop the jar in `plugins/`, restart, edit `plugins/MTREEPlayerIdentity/config.yml`, then `/mtree reload`.

## Commands (permission `mtree.admin`)
- `/mtree reload`
- `/mtree nametag <player>`  re-applies a player's nametag and shows their rank
- `/mtree intro <player>`
- `/mtree preview`  plays the intro on yourself

## Ranks
Each key under `ranks:` is matched to the player's LuckPerms **primary group** (or the group in `luckperms-group`).
Unknown groups, or no LuckPerms, use `default-rank`. Rank changes update live through a LuckPerms event, not polling.
Nametag colour comes from `name-color` and must be one of the 16 vanilla colours (team colours limitation).

## Performance
Nametags = scoreboard teams on the main scoreboard (`mt_0`, `mt_1`, ...), updated on join, quit, rank change, reload.
Intro = 4 one-shot tasks per player, cancelled when finished or on quit. Nothing else is scheduled.

## Compatibility
- Essentials: join message untouched by default. If you enable it, set Essentials' join message to `none` to avoid
  mixed output; ours is set at HIGHEST priority so only one message is sent.
- Plugins that also put players into scoreboard teams or set TAB names (e.g. Essentials nicknames) can override
  each other; the last writer wins until the next refresh.
- Bedrock via Geyser/Floodgate: titles, subtitles and sounds are translated by Geyser. Team prefixes/suffixes on
  nametags are supported by recent Geyser builds; if yours does not show them, the TAB list still does.
- Intro uses only Title/Subtitle/Sound; no resource pack.
