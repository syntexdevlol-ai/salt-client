# Salt Client (Fabric 1.21.1)

Client-side HUD/utility mod intended to run fine on Pojav Launcher (Android) as well as desktop.

## Install
- Put `salt-client-blurfix.1.0.0.jar` in your `mods/` folder.
- Requires Fabric Loader + Fabric API for `1.21.1`.

## Controls
- Open menu: `Right Shift`
- Zoom (when Zoom module is enabled): hold `C`
- Perspective (when enabled): hold `V`
- FreeLook (when enabled): hold `B`
- In the menu: left click = toggle, right click = settings (if available)

## Notes
- The full requested module list is present in the menu.
- Most "performance/optimizer/culling" modules are implemented in a Pojav-friendly way via vanilla option tweaks (lite), plus a few safe mixins.
- Client-side only (no packet spam / no risky automation).
- To use the in-game custom crosshair, enable `CustomCrosshair` (then right click it for the editor).

## Build (Termux / Linux / Windows)
```sh
./gradlew --no-daemon build
```

On Android `/sdcard/` (no executable bit), use:
```sh
sh gradlew --no-daemon build
```

Output:
- `build/libs/salt-client-1.0.0.jar` (you can rename it to `salt-client-blurfix.1.0.0.jar`)
