package com.proxlock.installer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.InputStream;
import java.util.Locale;

public class InstallerActivity extends Activity {
    private static final String ACTION_PERM = "com.proxlock.installer.USB_PERMISSION";
    private TextView status;
    private Button btnInstall, btnRemove;
    private UsbManager usb;
    private Runnable pending;
    private volatile boolean busy;

    private final BroadcastReceiver permReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) {
            if (!ACTION_PERM.equals(i.getAction())) return;
            Runnable r = pending; pending = null;
            if (i.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && r != null) r.run();
            else done(T.get("noperm"));
        }
    };

    private TextView steps;
    private final Button[] langButtons = new Button[T.LANGS.length];

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        usb = (UsbManager) getSystemService(USB_SERVICE);
        T.lang = getSharedPreferences("p", 0).getInt("lang", T.systemLang());
        int p = (int) (16 * getResources().getDisplayMetrics().density);

        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(p, p, p, p);

        // Language selector: 한국어 / English / 日本語 / 中文
        LinearLayout langRow = new LinearLayout(this);
        langRow.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < T.LANGS.length; i++) {
            final int idx = i;
            Button lb = new Button(this);
            lb.setText(T.LANGS[i]);
            lb.setAllCaps(false);
            lb.setOnClickListener(v -> setLang(idx));
            langRow.addView(lb, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            langButtons[i] = lb;
        }

        TextView title = new TextView(this);
        title.setText("ProxLock Installer");
        title.setTextSize(24);
        title.setPadding(0, p, 0, 0);
        steps = new TextView(this);
        steps.setTextSize(15);
        steps.setPadding(0, p, 0, p);
        btnInstall = new Button(this); btnInstall.setAllCaps(false);
        btnRemove = new Button(this); btnRemove.setAllCaps(false);
        status = new TextView(this);
        status.setTextSize(17);
        status.setPadding(0, p, 0, 0);
        l.addView(langRow); l.addView(title); l.addView(steps); l.addView(btnInstall); l.addView(btnRemove); l.addView(status);

        android.widget.ScrollView sv = new android.widget.ScrollView(this);
        sv.addView(l);
        setContentView(sv);
        btnInstall.setOnClickListener(v -> withQuest(this::doInstall));
        btnRemove.setOnClickListener(v -> withQuest(this::doRemove));
        refreshTexts();
        if (!getSharedPreferences("p", 0).contains("lang")) {
            // First launch: ask for the language before anything else.
            new android.app.AlertDialog.Builder(this)
                .setTitle("Language / 언어 / 言語 / 语言")
                .setItems(T.LANGS, (d, which) -> setLang(which))
                .setCancelable(false)
                .show();
        }
        IntentFilter f = new IntentFilter(ACTION_PERM);
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(permReceiver, f, 4 /* Context.RECEIVER_NOT_EXPORTED */);
        else registerReceiver(permReceiver, f);
    }

    @Override protected void onDestroy() { super.onDestroy(); unregisterReceiver(permReceiver); }

    private void setLang(int idx) {
        T.lang = idx;
        getSharedPreferences("p", 0).edit().putInt("lang", idx).apply();
        refreshTexts();
        if (!busy) status.setText("");
    }

    private void refreshTexts() {
        steps.setText(T.get("steps"));
        btnInstall.setText(T.get("install"));
        btnRemove.setText(T.get("remove"));
        for (int i = 0; i < langButtons.length; i++) langButtons[i].setEnabled(i != T.lang);
    }

    private interface Job { void run(Quest q) throws Exception; }

    private UsbDevice findQuest() {
        for (UsbDevice d : usb.getDeviceList().values())
            if (UsbTransport.findAdbInterface(d) != null) return d;
        return null;
    }

    private void withQuest(Job job) {
        if (busy) return;
        UsbDevice dev = findQuest();
        if (dev == null) { say(T.get("nodevice")); return; }
        Runnable start = () -> run(dev, job);
        if (usb.hasPermission(dev)) { start.run(); return; }
        pending = start;
        Intent i = new Intent(ACTION_PERM).setPackage(getPackageName());
        int flags = Build.VERSION.SDK_INT >= 31 ? PendingIntent.FLAG_MUTABLE : 0;
        usb.requestPermission(dev, PendingIntent.getBroadcast(this, 0, i, flags));
    }

    private void run(UsbDevice dev, Job job) {
        busy = true;
        setButtons(false);
        new Thread(() -> {
            UsbTransport t = null; Quest q = null;
            try {
                t = new UsbTransport(usb, dev);
                say(T.get("allow"));
                q = Quest.connect(t, Quest.crypto(this));
                job.run(q);
            } catch (Exception e) {
                done(T.get("fail") + " " + e.getMessage());
            } finally {
                if (q != null) q.close();
                if (t != null) t.close();
                busy = false;
                runOnUiThread(() -> setButtons(true));
            }
        }).start();
    }

    private void doInstall(Quest q) throws Exception {
        say(T.get("installing"));
        try (AssetFileDescriptor fd = getAssets().openFd("ProxLock.apk"); InputStream in = fd.createInputStream()) {
            q.install(in, (int) fd.getLength());
        }
        q.arm();
        for (int i = 0; i < 180; i++) {
            if (q.launchedOnce()) { done(T.get("ok")); return; }
            if (i == 2) say(T.get("wear"));
            Thread.sleep(1000);
        }
        done(T.get("timeout"));
    }

    private void doRemove(Quest q) throws Exception {
        if (!q.installed()) { done(T.get("notinst")); return; }
        q.uninstall();
        done(T.get("removed"));
    }

    private void setButtons(boolean on) { btnInstall.setEnabled(on); btnRemove.setEnabled(on); }
    private void say(String s) { runOnUiThread(() -> status.setText(s)); }
    private void done(String s) { say(s); }

    /** UI text in English, Korean, Japanese and Chinese. */
    static final class T {
        /** Order matches the columns passed to put(): en, ko, ja, zh. Shown as language buttons. */
        static final String[] LANGS = {"English", "한국어", "日本語", "中文"};
        static volatile int lang = 0;
        static int systemLang() {
            String l = Locale.getDefault().getLanguage();
            return "ko".equals(l) ? 1 : "ja".equals(l) ? 2 : "zh".equals(l) ? 3 : 0;
        }
        static String get(String k) {
            String[] v = M.get(k);
            return v == null ? k : v[lang];
        }
        private static final java.util.HashMap<String, String[]> M = new java.util.HashMap<>();
        private static void put(String k, String en, String ko, String ja, String zh) { M.put(k, new String[]{en, ko, ja, zh}); }
        static {
            put("steps",
                "1. Turn on Developer Mode for the Quest (Meta Horizon app → Devices → Headset settings → Developer Mode).\n\n"
                + "2. Connect this phone to the Quest with a USB cable (USB-C to USB-C, or an OTG adapter).\n\n"
                + "3. Tap [Install ProxLock] below.\n\n"
                + "4. On the phone: when asked to allow access to the USB device, tap [OK].\n\n"
                + "5. In the Quest: put on the headset. A notification \"Allow USB debugging?\" appears (if you don't see it, open the notifications panel). Select it, tick \"Always allow from this computer\" and tap [Allow].\n\n"
                + "6. Keep wearing the headset until the ProxLock window appears. When this screen shows \"Done!\", you can unplug the cable.",
                "1. Quest의 개발자 모드를 켜세요 (Meta Horizon 앱 → 기기 → 헤드셋 설정 → 개발자 모드).\n\n"
                + "2. 이 폰과 Quest를 USB 케이블(C to C 또는 OTG 젠더)로 연결하세요.\n\n"
                + "3. 아래 [ProxLock 설치] 버튼을 누르세요.\n\n"
                + "4. 폰에서 USB 기기 접근을 허용할지 물으면 [확인]을 누르세요.\n\n"
                + "5. Quest를 쓰세요. \"USB 디버깅을 허용하시겠습니까?\" 알림이 뜹니다 (안 보이면 알림창을 여세요). 알림을 선택한 뒤 \"이 컴퓨터에서 항상 허용\"을 체크하고 [허용]을 누르세요.\n\n"
                + "6. ProxLock 창이 뜰 때까지 헤드셋을 쓰고 있어 주세요. 이 화면에 \"완료!\"가 나오면 케이블을 빼도 됩니다.",
                "1. Quest の開発者モードをオンにしてください（Meta Horizon アプリ → デバイス → ヘッドセットの設定 → 開発者モード）。\n\n"
                + "2. このスマホと Quest を USB ケーブル（C to C または OTG アダプタ）で接続してください。\n\n"
                + "3. 下の［ProxLock をインストール］を押してください。\n\n"
                + "4. スマホで USB デバイスへのアクセス許可を求められたら［OK］を押してください。\n\n"
                + "5. Quest をかぶってください。「USB デバッグを許可しますか？」という通知が出ます（見えない場合は通知パネルを開いてください）。通知を選び、「このコンピューターを常に許可」にチェックして［許可］を押してください。\n\n"
                + "6. ProxLock の画面が出るまでヘッドセットをかぶったままにしてください。この画面に「完了！」と出たらケーブルを外してかまいません。",
                "1. 开启 Quest 的开发者模式（Meta Horizon 应用 → 设备 → 头显设置 → 开发者模式）。\n\n"
                + "2. 用 USB 数据线（C to C 或 OTG 转接头）连接手机和 Quest。\n\n"
                + "3. 点击下方的［安装 ProxLock］。\n\n"
                + "4. 手机询问是否允许访问 USB 设备时，点击［确定］。\n\n"
                + "5. 戴上 Quest，会出现\"允许 USB 调试吗？\"的通知（看不到时请打开通知面板）。选择该通知，勾选\"始终允许此计算机\"，然后点击［允许］。\n\n"
                + "6. 请一直戴着头显，直到出现 ProxLock 窗口。本页面显示\"完成！\"后即可拔掉数据线。");
            put("install", "Install ProxLock", "ProxLock 설치", "ProxLock をインストール", "安装 ProxLock");
            put("remove", "Remove ProxLock", "ProxLock 삭제", "ProxLock を削除", "删除 ProxLock");
            put("nodevice", "No Quest found. Check the cable and that Developer Mode is on.",
                "Quest를 찾지 못했습니다. 케이블과 개발자 모드가 켜져 있는지 확인하세요.",
                "Quest が見つかりません。ケーブルと開発者モードを確認してください。",
                "未找到 Quest。请检查数据线以及是否已开启开发者模式。");
            put("noperm", "USB access was not allowed. Tap the button again and allow it.",
                "USB 접근이 허용되지 않았습니다. 버튼을 다시 누르고 허용해 주세요.",
                "USB へのアクセスが許可されませんでした。もう一度ボタンを押して許可してください。",
                "未允许 USB 访问。请再次点击按钮并允许。");
            put("allow", "Put on the Quest. Open the \"Allow USB debugging?\" notification, tick \"Always allow\" and tap [Allow].",
                "Quest를 쓰고 \"USB 디버깅 허용\" 알림을 눌러 \"항상 허용\"을 체크한 뒤 [허용]을 누르세요.",
                "Quest をかぶり、「USB デバッグを許可」の通知を開いて「常に許可」にチェックし、［許可］を押してください。",
                "请戴上 Quest，打开\"允许 USB 调试\"通知，勾选\"始终允许\"并点击［允许］。");
            put("installing", "Installing ProxLock...", "ProxLock 설치 중...", "ProxLock をインストール中...", "正在安装 ProxLock...");
            put("wear", "Put on the headset once. Setup finishes when the ProxLock window appears (waiting up to 3 minutes).",
                "헤드셋을 한 번 써 주세요. ProxLock 창이 뜨면 설정이 끝납니다 (최대 3분 기다림).",
                "ヘッドセットを一度かぶってください。ProxLock の画面が出たら設定完了です（最大 3 分待ちます）。",
                "请戴上头显一次。出现 ProxLock 窗口即完成设置（最多等待 3 分钟）。");
            put("ok", "Done! The proximity sensor stays disabled, even after reboots. You can unplug the cable.\n\nPress the power button to sleep the headset when you take it off.",
                "완료! 재부팅해도 근접 센서가 계속 꺼진 상태로 유지됩니다. 케이블을 빼도 됩니다.\n\n헤드셋을 벗으면 전원 버튼으로 화면을 꺼 주세요.",
                "完了！再起動後も近接センサーは無効のままです。ケーブルを外してかまいません。\n\nヘッドセットを外したら電源ボタンでスリープしてください。",
                "完成！重启后距离传感器也会保持禁用。可以拔掉数据线了。\n\n摘下头显后请按电源键让其休眠。");
            put("timeout", "The app was not opened. In the headset, open Library > Unknown Sources > ProxLock once.",
                "앱이 열리지 않았습니다. 헤드셋에서 라이브러리 > 알 수 없는 출처 > ProxLock을 한 번 실행해 주세요.",
                "アプリが起動しませんでした。ヘッドセットでライブラリ > 提供元不明 > ProxLock を一度起動してください。",
                "应用未被打开。请在头显中打开一次：应用库 > 未知来源 > ProxLock。");
            put("removed", "ProxLock removed. The sensor works normally again.", "ProxLock을 삭제했습니다. 센서가 원래대로 작동합니다.",
                "ProxLock を削除しました。センサーは通常どおり動作します。", "已删除 ProxLock，传感器恢复正常。");
            put("notinst", "ProxLock is not installed.", "ProxLock이 설치되어 있지 않습니다.", "ProxLock はインストールされていません。", "未安装 ProxLock。");
            put("fail", "Failed:", "실패:", "失敗:", "失败：");
        }
    }
}
