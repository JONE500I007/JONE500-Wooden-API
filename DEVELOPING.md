# Developing Wooden API

Notes for working on Wooden API itself. If you only want to **use** Wooden API in your mod, see
[README.md](README.md) instead.

## Project layout

| Folder | What it is |
|---|---|
| `src/main` | The library itself (the only thing in the published jar) |
| `src/tutorial` | The tutorial mod: how to use Wooden API, one lesson per file (start with `TutorialMod.java`) |
| `src/tutorial/generated` | Data generation output of the tutorial (do not edit by hand) |

## Gradle tasks

| Gradle task | What it does |
|---|---|
| `runTutorialClient` | Minecraft with Wooden API + the tutorial woods (maple, willow, reed, ember) |
| `runDatagen` | Runs data generation for the tutorial (output: `src/tutorial/generated`) |
| `build` | Builds `build/libs/jone500-wooden-api-<version>.jar` (tutorial not included) |
| `publishToMavenLocal` | Publishes to `~/.m2` so other mods on this computer can use `mavenLocal()` |

## Releasing a new version (Modrinth)

Wooden API is distributed through Modrinth, which also serves every uploaded version as a maven
artifact (`maven.modrinth:jone500-wooden-api:<version>`).

1. Change `mod_version` in `gradle.properties` (e.g. `1.0.1`).
2. Run `gradlew runTutorialClient` and check the signs, boats and shelf in game.
3. Run `gradlew build`.
4. On Modrinth, create a new version of the `jone500-wooden-api` project with the **same version number**
   as the jar (e.g. `1.0.1+26.2`), tick the matching game version, and upload
   `build/libs/jone500-wooden-api-<version>.jar` as the primary file.
5. Do the same on every Minecraft branch (`1.21.9`, `1.21.10`, `1.21.11`, `26.1`, `26.2`, ...).

The full version number is `<mod_version>+<minecraft_version>`. `build.gradle` puts it together, so
you only ever edit `mod_version`; the jar name and `fabric.mod.json` get the full number
automatically. The version number on Modrinth is what modders put in
`maven.modrinth:jone500-wooden-api:<version>`, so it must match the jar exactly.

### Choosing the version number

Mods that use Wooden API may run with a **newer** Wooden API than the one they were built with
(players update it, or another mod ships a newer copy). So pick the number by what changed:

| What you changed | Example | New `mod_version` |
|---|---|---|
| Fixed a bug, no method added/removed/renamed | sign texture fix | `1.0.0` → `1.0.1` |
| Added something, nothing removed or renamed | new `.door()` method, new getter | `1.0.0` → `1.1.0` |
| Removed or renamed a public method/class, or changed its parameters | `.signs()` → `.addSigns()` | `1.x.x` → `2.0.0` |

The first two are safe: old mods keep working. The last one breaks mods built for `1.x`
(the game crashes with `NoSuchMethodError`), so avoid it when you can:

- To rename something, add the new name and keep the old one marked `@Deprecated`, calling the new
  one. Remove the old name only in the next major version (`2.0.0`).
- Write what changed in the Modrinth changelog, especially anything deprecated or removed.

### New Minecraft versions

Minecraft updates usually change code that Wooden API uses, so each Minecraft version needs its
own build:

1. Update `minecraft_version`, `loader_version` and `fabric_api_version` in `gradle.properties`
   (values from <https://fabricmc.net/develop>), and `"minecraft"` in
   `src/main/resources/fabric.mod.json`.
2. Fix compile errors. Then run `gradlew runDatagen` and `gradlew runTutorialClient`, and check in
   game that the signs, boats and shelf still look right.
3. The Minecraft part of the version number (`+26.2`) comes from `minecraft_version`, so it
   changes by itself. Each Minecraft version lives on its own git branch
   (`1.21.9`, `1.21.10`, `1.21.11`, `26.1`, `26.2`, ...).
4. Update the Minecraft version in the README examples (`1.0.0+26.2`, the Minecraft jar path).

### 1.21.x branches vs 26.x branches

Minecraft 26.1 is no longer obfuscated, so the two kinds of branches are set up differently:

| | 1.21.x branches | 26.x branches |
|---|---|---|
| Loom plugin | `net.fabricmc.fabric-loom-remap` | `net.fabricmc.fabric-loom` |
| Names in the code | Yarn (`yarn_mappings` in `gradle.properties`) | Mojang's own names (no mappings line) |
| Dependencies | `modImplementation` | `implementation` |
| Java | 21 | 25 |

A fix made on one kind of branch has to be rewritten with the other names when it is copied to the
other kind.
