# ProxLock

**English** · [한국어](README.ko.md) · [日本語](README.ja.md) · [中文](README.zh-CN.md)

Keeps the Meta Quest proximity sensor **disabled permanently, even after reboots**, just like covering the sensor with tape.

Meta Quest Developer Hub can only disable the sensor for 2/4/8 hours, and every method (MQDH, SideQuest, `adb`) resets on reboot. ProxLock is a tiny on-headset app that re-applies the setting at every boot and every 5 minutes.

**Why?** When the headset sleeps (taken off, standby) and wakes up, PC streaming apps like Virtual Desktop can start juddering/wobbling until restarted. Keeping the headset awake avoids it.

## Supported devices

| Device | Status |
|---|---|
| Quest Pro | ✅ Tested (incl. reboot) |
| Quest 3 / 3S / 2 | Should work (same system command), not yet tested. Reports welcome |

## Install (Windows PC, one click)

1. Turn on **Developer Mode**: Meta Horizon phone app → Devices → your headset → Headset settings → Developer Mode.
2. Download `ProxLock-vX.Y.Z.zip` from [Releases](../../releases) and unzip it.
3. Connect the Quest to the PC with a USB cable (data-capable).
4. Double-click **`install.bat`**. If asked inside the headset, tick **"Always allow from this computer"** and press **Allow**.
5. Done. You can unplug the cable.

If `adb` isn't installed, the installer automatically downloads Google's official Android platform-tools from `dl.google.com` (by using it you accept the [Android SDK terms](https://developer.android.com/studio/terms)).

## Install from an Android phone (no PC, untested)

1. Enable Developer Mode (step 1 above).
2. Install a USB ADB app on the phone (e.g. *Bugjaeger*) and download `ProxLock.apk` to the phone.
3. Connect phone ↔ Quest with a USB-C cable, allow USB debugging in the headset, and install the APK from the app.
4. **Open ProxLock once in the headset** (Library → filter → *Unknown Sources*). This is required for it to run at boot.

## Install manually (adb)

```
adb install -r ProxLock.apk
adb shell am start -n com.proxlock/.MainActivity
```

## Using it

- Nothing to do: it runs at boot by itself.
- To find the app: Library → top-right filter → **Unknown Sources** → ProxLock.
- The **Sensor ON** button in the app restores normal behavior and stops auto-disabling. **Sensor OFF** turns ProxLock back on.

## Uninstall

Double-click `uninstall.bat` (restores the sensor and removes the app), or uninstall ProxLock from Settings → Apps and reboot.

## ⚠️ Please note

- The screen **stays on** when you take the headset off. Press the power button to sleep it, or the battery will drain.
- **Never leave the lenses facing sunlight.** With the display on, this matters even more.
- If you turn the sensor back on with MQDH/SideQuest, ProxLock turns it off again within 5 minutes. Use the in-app **Sensor ON** button instead.
- Meta may change this behavior in a future update. If so, ProxLock simply stops having an effect.

## How it works / safety

- Sends `com.oculus.vrpowermanager.prox_close` (the same command MQDH and Meta's docs use) on `BOOT_COMPLETED` and via an inexact 5-minute alarm that does not wake the device.
- Only permission: `RECEIVE_BOOT_COMPLETED`. No internet, storage, or other permissions. About 10 KB, no background service.
- Source is in `app/`. Build with `build.sh` (Android SDK + JDK, no Gradle).

## Disclaimer

Not affiliated with or endorsed by Meta. "Meta Quest" is a trademark of Meta Platforms, Inc. Use at your own risk.

## License

[MIT](LICENSE)
