# ProxLock

[English](README.md) · [한국어](README.ko.md) · [日本語](README.ja.md) · **中文**

让 Meta Quest 的距离传感器**永久保持禁用，重启后也不会恢复**，效果和用胶带遮住传感器一样：摘下头显后也不会进入休眠。

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
5. 安装程序提示时，**请戴上头显一次**。出现 ProxLock 窗口即表示设置完成。
6. 完成，可以拔掉数据线了。

没有电脑？请参阅[其他安装方法](#其他安装方法)。

## 使用方法

- 无需任何操作，每次开机都会自动运行。
- 应用位置：应用库 → 右上角筛选 → **未知来源** → ProxLock
- **Sensor ON** 按钮：传感器恢复正常，并停止自动禁用。**Sensor OFF**：重新启用 ProxLock。

## ⚠️ 注意事项

- 摘下头显后**屏幕会保持点亮**。请按电源键让它休眠，否则会耗电。
- **切勿让镜片受到阳光直射**。屏幕点亮时更要注意。
- 要恢复传感器，请使用应用内的 **Sensor ON** 按钮。如果用 MQDH 或 SideQuest 开启，ProxLock 会在 5 分钟内再次将其关闭。

## 卸载

双击 `uninstall.bat`，会恢复传感器并删除应用。也可以在设置 → 应用中卸载 ProxLock，然后重启。

## 遇到问题？

如果出现问题（例如 Quest 系统更新后不再起作用），请提交 [Issue](../../issues)。

---

<br>

# 参考资料

*背景说明和技术细节。使用 ProxLock 不需要了解这些内容。*

## 其他安装方法

**用安卓手机安装（无需电脑，未测试）**

1. 开启开发者模式（安装第 1 步）。
2. 在安卓手机上安装 USB ADB 应用（例如 *Bugjaeger*），并把 `ProxLock.apk` 下载到手机。
3. 用 USB-C 数据线连接手机和 Quest，在头显中允许 USB 调试，然后用该应用安装 APK。
4. **在头显中打开一次 ProxLock**（应用库 → 筛选 → *未知来源*）。必须先打开一次，开机时才会自动运行。

**手动安装（adb）**

```
adb install -r ProxLock.apk
adb shell am start -n com.proxlock/.MainActivity
```
然后戴上头显，让应用打开一次。

**关于 adb：** 如果电脑上没有 `adb`，`install.bat` 会自动从 Google 官方服务器（`dl.google.com`）下载 Android platform-tools。使用即表示你同意 [Android SDK 条款](https://developer.android.com/studio/terms)。

## 开发原因

我在 Quest Pro 上使用 Virtual Desktop 时，只要摘下头显再戴上，或者短暂进入待机后再回来，**画面就会严重抖动**，只有退出并重新打开 Virtual Desktop 才能解决。原因很可能是头显进入了休眠（见下文），所以我开发了这个应用，来尝试最直接的解决办法：**让头显永远不休眠**，也就是禁用距离传感器。

Meta Quest Developer Hub（MQDH）里虽然有禁用传感器的开关，但并不能真正解决问题：

![MQDH 的 Device actions：Proximity Sensor 显示 "Enabled"，旁边有 Disable 按钮](docs/images/mqdh-proximity-sensor.png)

- **有时间限制。** 点击 *Disable* 后只能选择 **2 小时 / 4 小时 / 8 小时**，没有"永久禁用"选项。即使不重启，**时间一到设置也会自动失效**，头显又会开始休眠。

  ![MQDH 的 Disable 菜单：For 2 Hours / For 4 Hours / For 8 Hours](docs/images/mqdh-time-limit.png)

- **重启后会重置。** 头显重启后传感器会恢复开启，每次都得连上电脑重新点 Disable。SideQuest 和 `adb` 命令也一样。

ProxLock 解决了这两个限制：它会在每次开机时自动禁用传感器，并每 5 分钟重新应用一次。首次安装之后就不再需要电脑。

## 休眠后 Virtual Desktop 画面抖动

**症状：** 在 Quest 上使用 Virtual Desktop（VD）时，摘下头显再戴上，或短暂进入待机后再回来，画面会持续抖动，直到重启 VD。

**可能的原因**（这是无线 PC 串流的一般行为，并非在所有环境下实测的结果）：

1. **帧时序错乱**：头显休眠时，解码器和显示循环会停止，但电脑端的串流程序仍沿用之前的时序。唤醒后帧会延迟或集中到达，重投影（时间扭曲）不断修正，看起来就是画面抖动。
2. **SSW/ASW 一直开着**：唤醒瞬间短暂掉帧会触发 SSW（Synchronous SpaceWarp），有时之后不会自动关闭。于是画面变成半帧率插帧，物体边缘出现波纹。
3. **追踪原点被重置**：唤醒时 Quest 可能会重新设定边界或原点，导致位置预测暂时不稳。用 SpaceSync 校准过的 Lighthouse 追踪器位置也可能因此偏移。

重启 VD 能解决问题，是因为串流会话会重新建立，所有时序都从头重新同步。

**除了 ProxLock 之外的方法：** 不必完全退出 VD，在菜单中点 **Disconnect** 后重新连接；在 VD 设置中把 **SSW** 设为 *Disabled* 或 *Automatic*；让头显固定连接一个 Wi-Fi 频段（5GHz 或 6GHz）；保持 VD 应用和 Streamer 为最新版本。

> **测试结果（Quest Pro）：** 摘下头显后不再进入休眠，因此没有出现画面抖动。**但如果按电源键进入待机再唤醒，同样的抖动还会出现。** 也就是说，ProxLock 能在日常使用中避开这个问题，但并不能从根本上解决 VD 本身的问题。按电源键待机并唤醒后，请在 VD 中 **Disconnect → 重新连接**。

## 工作原理 / 安全性

- 在开机时（`BOOT_COMPLETED`）以及每 5 分钟发送一次 `com.oculus.vrpowermanager.prox_close`，与 MQDH 和 Meta 官方文档使用的命令相同。5 分钟的闹钟不会唤醒休眠中的设备。
- Android 只会把开机通知发给至少打开过一次的应用，所以安装程序会提示你戴上头显一次。
- 只申请了 `RECEIVE_BOOT_COMPLETED` 一个权限，没有网络、存储等其他权限。大小约 10KB，没有常驻后台服务。
- 源代码在 `app/` 目录中，用 `build.sh` 构建（Android SDK + JDK，无需 Gradle）。

## 免责声明

本项目与 Meta 无关，也未获得 Meta 的认可。"Meta Quest" 是 Meta Platforms, Inc. 的商标。使用风险自负。

## 许可证

[MIT](LICENSE)
