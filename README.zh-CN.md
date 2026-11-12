# ProxLock

[English](README.md) · [한국어](README.ko.md) · [日本語](README.ja.md) · **中文**

让 Meta Quest 的距离传感器**永久保持禁用，重启后也不会恢复**，效果和用胶带遮住传感器一样。

Meta Quest Developer Hub 只能把传感器禁用 2/4/8 小时，而 MQDH、SideQuest、`adb` 等任何方法在重启后都会失效。ProxLock 是一个运行在头显里的小应用，每次开机以及每隔 5 分钟都会重新应用这个设置。

**为什么需要？** 摘下头显或从待机中唤醒后，Virtual Desktop 等 PC 串流应用可能出现画面抖动，只有重启应用才能恢复。让头显不进入休眠就能避免这个问题。

## 支持的设备

| 设备 | 状态 |
|---|---|
| Quest Pro | ✅ 已测试（包括重启） |
| Quest 3 / 3S / 2 | 使用相同的系统命令，应该可以运行，但尚未测试，欢迎反馈 |

## 安装（Windows 电脑，一键安装）

1. **开启开发者模式**：手机上的 Meta Horizon 应用 → 设备 → 你的头显 → 头显设置 → 开发者模式
2. 从 [Releases](../../releases) 下载 `ProxLock-vX.Y.Z.zip` 并解压。
3. 用 USB 数据线（需支持数据传输）把 Quest 连接到电脑。
4. 双击 **`install.bat`**。如果头显里弹出提示，勾选 **"始终允许此计算机"** 并点击 **允许**。
5. 完成，可以拔掉数据线了。

如果电脑上没有 `adb`，安装程序会自动从 Google 官方服务器（`dl.google.com`）下载 Android platform-tools。使用即表示你同意 [Android SDK 条款](https://developer.android.com/studio/terms)。

## 用安卓手机安装（无需电脑，未测试）

1. 开启开发者模式（见上面第 1 步）。
2. 在安卓手机上安装 USB ADB 应用（例如 *Bugjaeger*），并把 `ProxLock.apk` 下载到手机。
3. 用 USB-C 数据线连接手机和 Quest，在头显中允许 USB 调试，然后用该应用安装 APK。
4. **在头显中打开一次 ProxLock**（应用库 → 筛选 → *未知来源*）。必须先打开一次，开机时才会自动运行。

## 手动安装（adb）

```
adb install -r ProxLock.apk
adb shell am start -n com.proxlock/.MainActivity
```

## 使用方法

- 无需任何操作，开机后自动运行。
- 应用位置：应用库 → 右上角筛选 → **未知来源** → ProxLock
- 点击应用内的 **Sensor ON** 可恢复正常，并停止自动禁用；点击 **Sensor OFF** 可重新启用 ProxLock。

## 卸载

双击 `uninstall.bat`，会恢复传感器并删除应用。也可以在设置 → 应用中卸载 ProxLock，然后重启。

## ⚠️ 注意事项

- 摘下头显后**屏幕会保持点亮**。请按电源键让它休眠，否则会耗电。
- **切勿让镜片受到阳光直射**。屏幕点亮时更要注意。
- 如果用 MQDH 或 SideQuest 重新开启传感器，ProxLock 会在 5 分钟内再次将其关闭。要恢复传感器，请使用应用内的 **Sensor ON** 按钮。
- 如果以后 Meta 的更新改变了这一行为，ProxLock 只会失去效果。

## 工作原理 / 安全性

- 在开机时（`BOOT_COMPLETED`）以及每 5 分钟发送一次 `com.oculus.vrpowermanager.prox_close`，与 MQDH 和 Meta 官方文档使用的命令相同。5 分钟的闹钟不会唤醒休眠中的设备。
- 只申请了 `RECEIVE_BOOT_COMPLETED` 一个权限，没有网络、存储等其他权限。大小约 10KB，没有常驻后台服务。
- 源代码在 `app/` 目录中，用 `build.sh` 构建（Android SDK + JDK，无需 Gradle）。

## 免责声明

本项目与 Meta 无关，也未获得 Meta 的认可。"Meta Quest" 是 Meta Platforms, Inc. 的商标。使用风险自负。

## 许可证

[MIT](LICENSE)
