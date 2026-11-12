# NoSleep for Quest

![センサーにテープ vs. NoSleep for Quest](docs/promo/card-tape.png)

[English](README.md) · [한국어](README.ko.md) · **日本語** · [中文](README.zh-CN.md)

Meta Quest の近接センサーを**再起動後も無効のまま維持**するアプリです。センサーをテープでふさいだのと同じ効果で、ヘッドセットを外してもスリープしなくなります。

## 対応デバイス

| デバイス | 状態 |
|---|---|
| Quest Pro | ✅ 動作確認済み（再起動を含む） |
| Quest 2 | ✅ 動作確認済み（再起動を含む） |
| Quest 3 / 3S | 同じシステムコマンドを使うため動作する見込みですが未検証です。報告を歓迎します |

## インストール

下の 2 つの方法から**どちらか 1 つ**を選んでください。

| | |
|---|---|
| 💻 **A. Windows PC** | [**NoSleep-for-Quest-v1.0.1.zip**](https://github.com/Leenamgi20/NoSleep-for-Quest/releases/latest/download/NoSleep-for-Quest-v1.0.1.zip) |
| 📱 **B. Android スマホ**（PC 不要） | [**NoSleep-Installer.apk**](https://github.com/Leenamgi20/NoSleep-for-Quest/releases/latest/download/NoSleep-Installer.apk) |

### A. Windows PC（ワンクリック）

1. **開発者モードをオン**：スマホの Meta Horizon アプリ → デバイス → ヘッドセット → ヘッドセットの設定 → 開発者モード
2. 上の表から **NoSleep-for-Quest-v1.0.1.zip** をダウンロードして展開します。
3. Quest を USB ケーブル（データ通信対応）で PC に接続します。
4. **`install.bat`** をダブルクリックします。
5. ヘッドセットをかぶり、**「USB デバッグを許可しますか？」** の通知を開きます（見えない場合は通知パネルを開いてください）。**「このコンピューターを常に許可」** にチェックして **「許可」** を押します。
6. NoSleep の画面が出るまでヘッドセットをかぶったままにしてください。インストーラーに **完了！** と出たらケーブルを外してかまいません。

![インストール完了時のインストーラー画面](docs/images/pc-installer-ja.png)

### B. Android スマホ（PC 不要）

1. **開発者モード** をオンにします（上と同じ）。
2. スマホで上の表から **NoSleep-Installer.apk** をダウンロードして開きます。
   - 「提供元不明のアプリ」の警告が出たら、ブラウザやファイルアプリにインストールを許可してください。
   - Google Play プロテクトが不明なアプリと表示した場合は、**詳細 → インストールする（無視してインストール）** を押してください。
3. **NoSleep Installer** を開き、言語を選びます。
4. スマホと Quest を USB ケーブル（C to C または OTG アダプタ）で接続します。
5. **NoSleep をインストール** を押します。スマホで USB デバイスへのアクセス許可を求められたら **OK** を押します。
6. ヘッドセットをかぶり、USB デバッグを許可します（PC インストールの手順 5 と同じ）。
7. NoSleep の画面が出るまでヘッドセットをかぶったままにしてください。スマホに **完了！** と出たらケーブルを外してかまいません。

<img src="docs/images/phone-ja.png" width="300" alt="Android スマホの NoSleep Installer 画面">

iPhone ではインストールできません（iOS はアプリが USB デバイスを操作することを許可していません）。iPhone の方は PC からインストールしてください。

## 使い方

- 何もする必要はありません。起動するたびに自動で動作します。
- アプリの場所：ライブラリ → 右上のフィルター → **提供元不明** → NoSleep
- **センサー ON** ボタン：センサーが通常どおり動作し、自動無効化も止まります。**センサー OFF**：NoSleep が再び有効になります。

<img src="docs/images/app-ja.png" width="340" alt="ヘッドセット内の NoSleep 画面">

## ⚠️ 注意

- ヘッドセットを外しても**画面は点いたまま**です。電源ボタンでスリープしないとバッテリーが減ります。
- **レンズに直射日光を当てないでください。** 画面が点いているので特に注意が必要です。
- センサーを元に戻すときはアプリの **センサー ON** ボタンを使ってください。MQDH や SideQuest で有効にしても、NoSleep が 5 分以内に再び無効にします。

## アンインストール

`uninstall.bat` をダブルクリックすると、センサーを元に戻してアプリを削除します。または設定 → アプリから NoSleep を削除して再起動してください。

## 問題があるとき

Quest のシステムアップデート後に動作しなくなった場合などは、[Issue](../../issues) でお知らせください。

---

<br>

# 参考資料

*背景説明と技術的な内容です。NoSleep を使うのに必要な情報ではありません。*

## その他のインストール方法

**手動インストール（adb）**

```
adb install -r NoSleep.apk
adb shell am start -n com.proxlock/.MainActivity
```
その後、ヘッドセットをかぶってアプリが一度開くようにしてください。

**adb について：** `adb` がない場合は、`install.bat` が Google 公式サーバー（`dl.google.com`）から Android platform-tools を自動でダウンロードします。使用することで [Android SDK 利用規約](https://developer.android.com/studio/terms) に同意したものとみなされます。

## 作った理由

Quest を少し外すたびにスタンバイに入り、かぶり直すたびに復帰と再接続を待つ必要がありました。解決策はシンプルで、**ヘッドセットをスリープさせないこと**、つまり近接センサーを無効にすることです。センサーにテープを貼ってこうしている人も多いですが、テープなしで同じ効果を得たくてこのアプリを作りました。

Meta Quest Developer Hub（MQDH）にもセンサーを無効にするボタンはありますが、問題を根本的には解決できません。

![MQDH の Device actions：Proximity Sensor が「Enabled」で、Disable ボタンがある](docs/images/mqdh-proximity-sensor.png)

- **時間制限があります。** *Disable* を押しても **2 時間 / 4 時間 / 8 時間** から選ぶことしかできず、「ずっと無効」はありません。再起動しなくても、**時間が過ぎると設定が自動的に解除され**、ヘッドセットはまたスリープするようになります。

  ![MQDH の Disable メニュー：For 2 Hours / For 4 Hours / For 8 Hours](docs/images/mqdh-time-limit.png)

- **再起動するとリセットされます。** ヘッドセットを再起動するとセンサーは元どおり有効になっているので、毎回 PC につないで Disable を押し直す必要があります。SideQuest や `adb` コマンドでも同じです。

NoSleep はこの 2 つの制限をなくします。起動するたびに自動でセンサーを無効にし、5 分ごとに再適用するので、最初のインストール後は PC も必要ありません。

## 仕組み / 安全性

- MQDH や Meta 公式ドキュメントと同じ `com.oculus.vrpowermanager.prox_close` コマンドを、起動時（`BOOT_COMPLETED`）と 5 分ごとに送ります。5 分ごとのアラームはスリープ中のデバイスを起こしません。
- Android は一度起動されたことのあるアプリにしか起動通知を送りません。そのため、インストーラーはヘッドセットを一度かぶるよう案内します。
- 権限は `RECEIVE_BOOT_COMPLETED` のみです。インターネット・ストレージなどの権限はありません。約 10KB で、常駐サービスもありません。
- ソースは `app/` にあります。`build.sh` でビルドします（Android SDK + JDK、Gradle 不要）。
- スマホ用インストーラー（`android-installer/`）は [AdbLib](https://github.com/cgutman/AdbLib)（BSD-3-Clause、ライセンス同梱）を使って USB 経由で ADB 通信をします。インターネット権限はなく、接続したヘッドセットに NoSleep をインストール・起動、または削除するだけです。NoSleep.apk はアプリに内蔵されています。

## 免責事項

Meta とは無関係で、Meta の承認も受けていません。「Meta Quest」は Meta Platforms, Inc. の商標です。自己責任でご利用ください。

## ライセンス

[MIT](LICENSE)
