package com.proxlock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

final class Prox {
    static final String CLOSE = "com.oculus.vrpowermanager.prox_close";
    static final String RESTORE = "com.oculus.vrpowermanager.automation_disable";
    static final long INTERVAL = 5 * 60 * 1000L; // re-assert every 5 min

    static SharedPreferences prefs(Context c) {
        return c.createDeviceProtectedStorageContext().getSharedPreferences("p", 0);
    }
    static boolean enabled(Context c) { return prefs(c).getBoolean("on", true); }

    static void apply(Context c) {
        if (enabled(c)) {
            c.sendBroadcast(new Intent(CLOSE));
            schedule(c);
        } else {
            cancel(c);
        }
    }
    static void setEnabled(Context c, boolean on) {
        prefs(c).edit().putBoolean("on", on).apply();
        if (on) apply(c);
        else { cancel(c); c.sendBroadcast(new Intent(RESTORE)); }
    }
    private static PendingIntent pi(Context c) {
        return PendingIntent.getBroadcast(c, 0, new Intent(c, AlarmReceiver.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }
    static void schedule(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + INTERVAL, INTERVAL, pi(c));
    }
    static void cancel(Context c) {
        ((AlarmManager) c.getSystemService(Context.ALARM_SERVICE)).cancel(pi(c));
    }
}
