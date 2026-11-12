# ProxLock

[English](README.md) · [한국어](README.ko.md) · **日本語** · [中文](README.zh-CN.md)

Meta Quest の近接センサーを**再起動後も無効のまま維持**するアプリです。センサーをテープでふさいだのと同じ効果があります。

Meta Quest Developer Hub ではセンサーを 2/4/8 時間しか無効にできず、MQDH・SideQuest・`adb` のどの方法でも再起動すると元に戻ります。ProxLock はヘッドセット上で動く小さなアプリで、起動時と 5 分ごとに設定を再適用します。

**なぜ必要？** ヘッドセットを外したりスリープから復帰したりすると、Virtual Desktop などの PC ストリーミングアプリで画面が揺れ、アプリを再起動するまで直らないことがあります。ヘッドセットをスリープさせないことでこれを防げます。

## 対応デバイス

| デバイス | 状態 |
|---|---|
| Quest Pro | ✅ 動作確認済み（再起動を含む） |
| Quest 3 / 3S / 2 | 同じシステムコマンドを使うため動作する見込みですが未検証です。報告を歓迎します |

## インストール（Windows PC・ワンクリック）

1. **開発者モードをオン**：スマホの Meta Horizon アプリ → デバイス → ヘッドセット → ヘッドセットの設定 → 開発者モード
2. [Releases](../../releases) から `ProxLock-vX.Y.Z.zip` をダウンロードして展開します。
3. Quest を USB ケーブル（データ通信対応）で PC に接続します。
4. **`install.bat`** をダブルクリックします。ヘッドセット内で確認が出たら **「このコンピューターを常に許可」** にチェックして **「許可」** を押します。
5. 完了です。ケーブルを外してかまいません。

`adb` がない場合は、Google 公式サーバー（`dl.google.com`）から Android platform-tools を自動でダウンロードします。使用することで [Android SDK 利用規約](https://developer.android.com/studio/terms) に同意したものとみなされます。

## スマホからインストール（PC 不要・未検証）

1. 開発者モードをオンにします（上記 1）。
2. Android スマホに USB ADB アプリ（例：*Bugjaeger*）を入れ、`ProxLock.apk` をスマホにダウンロードします。
3. スマホと Quest を USB-C ケーブルで接続し、ヘッドセットで USB デバッグを許可してから、アプリで APK をインストールします。
4. **ヘッドセットで ProxLock を一度起動**します（ライブラリ → フィルター → *提供元不明*）。一度起動しないと起動時に自動で動作しません。

## 手動インストール（adb）

```
adb install -r ProxLock.apk
adb shell am start -n com.proxlock/.MainActivity
```

## 使い方

- 何もする必要はありません。起動時に自動で動作します。
- アプリの場所：ライブラリ → 右上のフィルター → **提供元不明** → ProxLock
- アプリ内の **Sensor ON** を押すと通常の動作に戻り、自動無効化も止まります。**Sensor OFF** で ProxLock が再び有効になります。

## アンインストール

`uninstall.bat` をダブルクリックすると、センサーを元に戻してアプリを削除します。または設定 → アプリから ProxLock を削除して再起動してください。

## ⚠️ 注意

- ヘッドセットを外しても**画面は点いたまま**です。電源ボタンでスリープしないとバッテリーが減ります。
- **レンズに直射日光を当てないでください。** 画面が点いているので特に注意が必要です。
- MQDH や SideQuest でセンサーを有効に戻しても、ProxLock が 5 分以内に再び無効にします。センサーを戻すときはアプリの **Sensor ON** ボタンを使ってください。
- 今後の Meta のアップデートでこの動作が変わった場合、ProxLock は効果がなくなるだけです。

## 仕組み / 安全性

- MQDH や Meta 公式ドキュメントと同じ `com.oculus.vrpowermanager.prox_close` コマンドを、起動時（`BOOT_COMPLETED`）と 5 分ごとに送ります。5 分ごとのアラームはスリープ中のデバイスを起こしません。
- 権限は `RECEIVE_BOOT_COMPLETED` のみです。インターネット・ストレージなどの権限はありません。約 10KB で、常駐サービスもありません。
- ソースは `app/` にあります。`build.sh` でビルドします（Android SDK + JDK、Gradle 不要）。

## 免責事項

Meta とは無関係で、Meta の承認も受けていません。「Meta Quest」は Meta Platforms, Inc. の商標です。自己責任でご利用ください。

## ライセンス

[MIT](LICENSE)
