package com.proxlock;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
public class MainActivity extends Activity {
    private TextView status;
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);
        l.setPadding(48, 48, 48, 48);
        status = new TextView(this);
        status.setTextSize(22);
        Button off = new Button(this); off.setText("Sensor OFF (always awake)");
        Button on = new Button(this); on.setText("Sensor ON (restore default)");
        off.setOnClickListener(v -> { Prox.setEnabled(this, true); refresh(); });
        on.setOnClickListener(v -> { Prox.setEnabled(this, false); refresh(); });
        l.addView(status); l.addView(off); l.addView(on);
        setContentView(l);
        Prox.apply(this); // first launch also arms boot receiver
        refresh();
    }
    private void refresh() {
        status.setText(Prox.enabled(this)
            ? "ProxLock: ON\nSensor disabled, re-applied at boot + every 5 min"
            : "ProxLock: OFF\nSensor works normally");
    }
}
