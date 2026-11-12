package com.proxlock.installer;

import android.content.Context;
import android.util.Base64;

import com.cgutman.adblib.AdbBase64;
import com.cgutman.adblib.AdbConnection;
import com.cgutman.adblib.AdbCrypto;
import com.cgutman.adblib.AdbStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/** The only things this app does to the headset: install/launch NoSleep, or remove it. */
final class Quest {
    static final String PKG = "com.proxlock";
    private final AdbConnection adb;

    private Quest(AdbConnection adb) { this.adb = adb; }

    /** Loads (or creates once) this phone's ADB key so "Always allow" works next time. */
    static AdbCrypto crypto(Context c) throws Exception {
        AdbBase64 b64 = data -> Base64.encodeToString(data, Base64.NO_WRAP);
        File priv = new File(c.getFilesDir(), "adbkey"), pub = new File(c.getFilesDir(), "adbkey.pub");
        if (priv.exists() && pub.exists()) {
            try { return AdbCrypto.loadAdbKeyPair(b64, priv, pub); } catch (Exception ignored) {}
        }
        AdbCrypto k = AdbCrypto.generateAdbKeyPair(b64);
        k.saveAdbKeyPair(priv, pub);
        return k;
    }

    /** Blocks until the headset accepts the connection (user taps "Allow" in the headset). */
    static Quest connect(UsbTransport t, AdbCrypto crypto) throws Exception {
        AdbConnection c = AdbConnection.create(t.in, t.out, t, crypto);
        c.connect();
        return new Quest(c);
    }

    String shell(String cmd) throws Exception {
        AdbStream s = adb.open("shell:" + cmd);
        return readAll(s);
    }

    private static String readAll(AdbStream s) throws InterruptedException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            while (!s.isClosed()) out.write(s.read());
        } catch (IOException ignored) { /* stream closed by remote: done */ }
        return out.toString();
    }

    void install(InputStream apk, int size) throws Exception {
        AdbStream s = adb.open("exec:cmd package install -r -S " + size);
        byte[] buf = new byte[Math.max(1024, adb.getMaxData() - 64)];
        int n;
        while ((n = apk.read(buf)) > 0) {
            byte[] chunk = n == buf.length ? buf.clone() : java.util.Arrays.copyOf(buf, n);
            s.write(chunk);
        }
        String result = readAll(s);
        if (!result.contains("Success")) throw new IOException(result.trim().isEmpty() ? "install failed" : result.trim());
    }

    /** Applies the setting now and queues the one-time launch Android needs before BOOT_COMPLETED. */
    void arm() throws Exception {
        shell("am broadcast -a com.oculus.vrpowermanager.prox_close");
        shell("am broadcast -a com.proxlock.ARM -n " + PKG + "/.BootReceiver -f 0x20");
        shell("am start -n " + PKG + "/.MainActivity");
    }

    boolean launchedOnce() throws Exception {
        return !shell("dumpsys package " + PKG).contains("stopped=true");
    }

    boolean installed() throws Exception {
        return shell("pm list packages " + PKG).matches("(?s).*(^|\\n)package:" + PKG.replace(".", "\\.") + "\\s*(\\n.*)?$");
    }

    void uninstall() throws Exception {
        shell("am broadcast -a com.oculus.vrpowermanager.automation_disable");
        shell("pm uninstall " + PKG);
    }

    void close() { try { adb.close(); } catch (Exception ignored) {} }
}
