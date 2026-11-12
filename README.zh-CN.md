# ProxLock

[English](README.md) · [한국어](README.ko.md) · [日本語](README.ja.md) · **中文**

让 Meta Quest 的距离传感器**永久保持禁用，重启后也不会恢复**，效果和用胶带遮住传感器一样。

Meta Quest Developer Hub 只能把传感器禁用 2/4/8 小时，而 MQDH、SideQuest、`adb` 等任何方法在重启后都会失效。ProxLock 是一个运行在头显里的小应用，每次开机以及每隔 5 分钟都会重新应用这个设置。

## 开发原因

我在 Quest Pro 上使用 Virtual Desktop 时，只要摘下头显再戴上，或者短暂进入待机后再回来，**画面就会严重抖动**，只有退出并重新打开 Virtual Desktop 才能解决。原因是头显进入了休眠：从休眠中唤醒后，串流的帧时序会错乱。所以最干净的解决办法就是**让头显永远不休眠**，也就是禁用距离传感器。

Meta Quest Developer Hub（MQDH）里虽然有禁用传感器的开关，但并不能真正解决问题：

![MQDH 的 Device actions：Proximity Sensor 显示 "Enabled"，旁边有 Disable 按钮](docs/images/mqdh-proximity-sensor.png)

- **有时间限制。** 点击 *Disable* 后只能选择 **2 小时 / 4 小时 / 8 小时**，没有"永久禁用"选项。即使不重启，**时间一到设置也会自动失效**，头显又会开始休眠。

  ![MQDH 的 Disable 菜单：For 2 Hours / For 4 Hours / For 8 Hours](docs/images/mqdh-time-limit.png)

- **重启后会重置。** 头显重启后传感器会恢复开启，每次都得连上电脑重新点 Disable。SideQuest 和 `adb` 命令也一样。

ProxLock 解决了这两个限制：它会在**每次开机时自动禁用传感器，并每 5 分钟重新应用一次**，效果就像用胶带遮住传感器一样。首次安装之后就不再需要电脑。

## 背景：休眠后 Virtual Desktop 画面抖动

**症状：** 在 Quest 上使用 Virtual Desktop（VD）时，摘下头显再戴上，或短暂进入待机后再回来，画面会持续抖动。只有退出并重新打开 VD 才能恢复。

**可能的原因**（这是无线 PC 串流的一般行为，并非在所有环境下实测的结果）：

1. **帧时序错乱**：头显休眠时，解码器和显示循环会停止，但电脑端的串流程序仍沿用之前的时序。唤醒后帧会延迟或集中到达，重投影（时间扭曲）不断修正，看起来就是画面抖动。
2. **SSW/ASW 一直开着**：唤醒瞬间短暂掉帧会触发 SSW（Synchronous SpaceWarp），有时之后不会自动关闭。于是画面变成半帧率插帧，物体边缘出现波纹。
3. **追踪原点被重置**：唤醒时 Quest 可能会重新设定边界或原点，导致位置预测暂时不稳。用 SpaceSync 校准过的 Lighthouse 追踪器位置也可能因此偏移。

重启 VD 能解决问题，是因为串流会话会重新建立，所有时序都从头重新同步。

**除了 ProxLock 之外的方法：**

- 不必完全退出 VD，在 VD 菜单中点 **Disconnect** 后重新连接即可。大多数情况下这样就能解决，而且快得多。
- 在 VD 设置中把 **SSW** 设为 *Disabled* 或 *Automatic*（避免 *Always On*）。
- 让头显固定连接一个 Wi-Fi 频段（5GHz 或 6GHz），避免唤醒时切换频段。
- 保持 VD 应用和 Streamer 为最新版本。

ProxLock 从根本上解决问题：头显不休眠，串流就不需要从休眠中恢复。

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
5. 安装程序提示时，**请戴上头显一次**。出现 ProxLock 窗口即表示设置完成。Android 只会在开机时自动运行至少打开过一次的应用。
6. 完成，可以拔掉数据线了。

如果 Quest 系统更新后不再起作用，请提交 [Issue](../../issues)。

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
