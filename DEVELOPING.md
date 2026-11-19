# Developing Wooden API

Notes for working on Wooden API itself. If you only want to **use** Wooden API in your mod, see
[README.md](README.md) instead.

## Project layout

| Folder | What it is |
|---|---|
| `src/main` | The library itself (the only thing in the published jar) |
| `src/testmod` | An example mod ("maple" wood) that uses Wooden API |
| `src/testmod/generated` | Data generation output of the testmod (do not edit by hand) |

## Gradle tasks

| Gradle task | What it does |
|---|---|
| `runTestmodClient` | Minecraft with Wooden API + the example maple wood |
| `runDatagen` | Runs data generation for the testmod (output: `src/testmod/generated`) |
| `build` | Builds `build/libs/jone500-wooden-api-<version>.jar` (testmod not included) |
| `publishToMavenLocal` | Publishes to `~/.m2` so other mods on this computer can use `mavenLocal()` |

## Releasing a new version (Modrinth)

Wooden API is distributed through Modrinth, which also serves every uploaded version as a maven
artifact (`maven.modrinth:jone500-wooden-api:<version>`).

1. Change `mod_version` in `gradle.properties` (e.g. `1.0.1`).
2. Run `gradlew runTestmodClient` and check the signs, boats and shelf in game.
3. Run `gradlew build`.
4. On Modrinth, create a new version of the `jone500-wooden-api` project with the **same version number**
   as the jar (e.g. `1.0.1+1.21.11`), tick the matching game version, and upload
   `build/libs/jone500-wooden-api-<version>.jar` as the primary file.
5. Do the same on every Minecraft branch (`1.21.9`, `1.21.10`, `1.21.11`, ...).

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

1. Update `minecraft_version`, `yarn_mappings`, `loader_version` and `fabric_api_version` in
   `gradle.properties` (values from <https://fabricmc.net/develop>), and `"minecraft"` in
   `src/main/resources/fabric.mod.json`.
2. Fix compile errors. Then run `gradlew runTestmodClient`, and check in game that the signs,
   boats and shelf still look right.
3. The Minecraft part of the version number (`+1.21.11`) comes from `minecraft_version`, so it
   changes by itself. Each Minecraft version lives on its own git branch (`1.21.9`, `1.21.10`, `1.21.11`, ...).
