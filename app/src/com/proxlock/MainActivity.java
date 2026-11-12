package com.proxlock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {
    // Columns: en, ko, ja, zh
    private static final String[] LANGS = {"English", "한국어", "日本語", "中文"};
    private static final String[][] TXT = {
        /* off   */ {"Sensor OFF (always awake)", "센서 끄기 (항상 켜짐)", "センサー OFF（常に起動）", "关闭传感器（始终唤醒）"},
        /* on    */ {"Sensor ON (restore default)", "센서 켜기 (원래대로)", "センサー ON（元に戻す）", "开启传感器（恢复默认）"},
        /* stOn  */ {"ProxLock: ON\nSensor disabled, re-applied at boot and every 5 min",
                     "ProxLock: 켜짐\n센서가 꺼져 있고, 부팅할 때와 5분마다 다시 적용됩니다",
                     "ProxLock: ON\nセンサーは無効です。起動時と 5 分ごとに再適用されます",
                     "ProxLock：开启\n传感器已禁用，开机时及每 5 分钟重新应用"},
        /* stOff */ {"ProxLock: OFF\nSensor works normally", "ProxLock: 꺼짐\n센서가 원래대로 작동합니다",
                     "ProxLock: OFF\nセンサーは通常どおり動作します", "ProxLock：关闭\n传感器正常工作"},
    };
    private static final int OFF = 0, ON = 1, ST_ON = 2, ST_OFF = 3;

    private TextView status;
    private Button btnOff, btnOn;
    private final Button[] langButtons = new Button[LANGS.length];
    private int lang;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        SharedPreferences p = Prox.prefs(this);
        lang = p.getInt("lang", systemLang());

        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);
        l.setPadding(48, 32, 48, 48);

        LinearLayout langRow = new LinearLayout(this);
        for (int i = 0; i < LANGS.length; i++) {
            final int idx = i;
            Button lb = new Button(this);
            lb.setText(LANGS[i]);
            lb.setAllCaps(false);
            lb.setOnClickListener(v -> setLang(idx));
            langRow.addView(lb, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            langButtons[i] = lb;
        }

        status = new TextView(this);
        status.setTextSize(22);
        status.setPadding(0, 32, 0, 32);
        btnOff = new Button(this); btnOff.setAllCaps(false);
        btnOn = new Button(this); btnOn.setAllCaps(false);
        btnOff.setOnClickListener(v -> { Prox.setEnabled(this, true); refresh(); });
        btnOn.setOnClickListener(v -> { Prox.setEnabled(this, false); refresh(); });
        l.addView(langRow, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        l.addView(status); l.addView(btnOff); l.addView(btnOn);
        setContentView(l);

        Prox.apply(this); // first launch also arms the boot receiver
        refresh();

        if (!p.contains("lang")) {
            new AlertDialog.Builder(this)
                .setTitle("Language / 언어 / 言語 / 语言")
                .setItems(LANGS, (d, which) -> setLang(which))
                .setCancelable(false)
                .show();
        }
    }

    private static int systemLang() {
        String l = Locale.getDefault().getLanguage();
        return "ko".equals(l) ? 1 : "ja".equals(l) ? 2 : "zh".equals(l) ? 3 : 0;
    }

    private void setLang(int idx) {
        lang = idx;
        Prox.prefs(this).edit().putInt("lang", idx).apply();
        refresh();
    }

    private void refresh() {
        status.setText(TXT[Prox.enabled(this) ? ST_ON : ST_OFF][lang]);
        btnOff.setText(TXT[OFF][lang]);
        btnOn.setText(TXT[ON][lang]);
        for (int i = 0; i < langButtons.length; i++) langButtons[i].setEnabled(i != lang);
    }
}
