# LumLib

A lightweight, dependency-free configuration screen library for Fabric client mods.

LumLib gives any client-side mod a polished config screen without requiring
Cloth Config, YACL, or ModMenu. If ModMenu is installed, your mod's config
button shows up there like normal. If it isn't, LumLib adds a small,
unobtrusive gear-icon button to the bottom-right corner of the pause screen
so players can still reach your settings.

## Features

- **Zero required dependencies** — only Fabric API, which almost every mod already uses.
- **ModMenu integration when present, automatic fallback button when not.**
- **6 widget types**: boolean, int slider, double slider, text input, enum/dropdown, color (with hex picker + alpha).
- **Automatic JSON serialization** for every registered config, no boilerplate.
- **20+ languages** out of the box, following Minecraft's own locale codes.
- **Multi-mod aware**: one LumLib button lists every installed mod that registers with it.
- **Tiny footprint** — no reflection-heavy option builders, no unused rendering paths.

## Adding LumLib to your mod

LumLib is not (yet) published to a public Maven repository. Until it is, publish it
to Maven Local and consume it as a Gradle dependency — do **not** ship a bare local jar.

### Publish to Maven Local

Run the matching publish task from this repo. Pingless currently uses all eight
target variants, so publish them together with:

```bash
./gradlew \
  :fabric-1.21-1.21.1:publishToMavenLocal \
  :fabric-1.21.2-1.21.5:publishToMavenLocal \
  :fabric-1.21.6-1.21.8:publishToMavenLocal \
  :fabric-1.21.9-1.21.10:publishToMavenLocal \
  :fabric-1.21.11:publishToMavenLocal \
  :fabric-26.1-26.1.2:publishToMavenLocal \
  :fabric-26.2:publishToMavenLocal \
  :fabric-26.3:publishToMavenLocal
```

In the consuming mod's `build.gradle`:

```groovy
repositories {
    mavenLocal()
}

dependencies {
    modImplementation 'dev.lumyrix:lumlib-fabric-1.21.11:1.0.0'
}
```

Match the artifact suffix (`fabric-1.21-1.21.1`, `fabric-1.21.2-1.21.5`, `fabric-1.21.6-1.21.8`, `fabric-1.21.9-1.21.10`, `fabric-1.21.11`, `fabric-26.1-26.1.2`, `fabric-26.2`, `fabric-26.3`) to the Minecraft version your mod targets.

### GitHub Packages / your own Maven host

Once you have a Maven host, add it as a `maven { url = '...' }` repository
and depend on the published coordinates the same way as Option B.

## Registering your mod's config

```java
public class MyModConfig {
    public static void register() {
        LumConfigContainer container = LumLib.registerMod("mymod", "My Mod");

        container.addBoolean("enabled", "Enabled", true, v -> Config.enabled = v, () -> Config.enabled);
        container.addIntSlider("range", "Range", 1, 64, 16, v -> Config.range = v, () -> Config.range);
        container.addColor("highlightColor", "Highlight Color", 0xFFFFFFFF, v -> Config.color = v, () -> Config.color);

        container.build();
    }
}
```

Call `MyModConfig.register()` from your client entrypoint. LumLib handles the
screen, the JSON file, and the ModMenu/fallback-button wiring automatically.

## License

See [LICENSE](LICENSE).
