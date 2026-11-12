package com.proxlock.installer;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** ADB over USB: wraps the bulk endpoints of the ADB interface as a stream pair for AdbLib. */
final class UsbTransport implements Closeable {
    static final int ADB_CLASS = 0xff, ADB_SUBCLASS = 0x42, ADB_PROTOCOL = 0x01;
    private static final int HEADER = 24;

    final InputStream in;
    final OutputStream out;
    private final UsbDeviceConnection conn;
    private final UsbInterface intf;

    static UsbInterface findAdbInterface(UsbDevice d) {
        for (int i = 0; i < d.getInterfaceCount(); i++) {
            UsbInterface f = d.getInterface(i);
            if (f.getInterfaceClass() == ADB_CLASS && f.getInterfaceSubclass() == ADB_SUBCLASS
                    && f.getInterfaceProtocol() == ADB_PROTOCOL) return f;
        }
        return null;
    }

    UsbTransport(UsbManager mgr, UsbDevice dev) throws IOException {
        intf = findAdbInterface(dev);
        if (intf == null) throw new IOException("No ADB interface (is Developer Mode on?)");
        UsbEndpoint epIn = null, epOut = null;
        for (int i = 0; i < intf.getEndpointCount(); i++) {
            UsbEndpoint e = intf.getEndpoint(i);
            if (e.getType() != UsbConstants.USB_ENDPOINT_XFER_BULK) continue;
            if (e.getDirection() == UsbConstants.USB_DIR_IN) epIn = e; else epOut = e;
        }
        if (epIn == null || epOut == null) throw new IOException("ADB endpoints not found");
        conn = mgr.openDevice(dev);
        if (conn == null) throw new IOException("Cannot open USB device");
        if (!conn.claimInterface(intf, true)) { conn.close(); throw new IOException("Cannot claim USB interface"); }
        final UsbEndpoint rIn = epIn, rOut = epOut;

        in = new InputStream() {
            private final byte[] buf = new byte[16384];
            private int pos, len;
            private boolean fill() throws IOException {
                while (pos >= len) {
                    int n = conn.bulkTransfer(rIn, buf, buf.length, 0);
                    if (n < 0) throw new IOException("USB read failed");
                    pos = 0; len = n;
                }
                return true;
            }
            @Override public int read() throws IOException { fill(); return buf[pos++] & 0xff; }
            @Override public int read(byte[] b, int off, int l) throws IOException {
                fill();
                int n = Math.min(l, len - pos);
                System.arraycopy(buf, pos, b, off, n);
                pos += n;
                return n;
            }
        };

        // AdbLib writes header+payload as one array; adbd expects them as separate USB transfers.
        out = new OutputStream() {
            private void send(byte[] b, int off, int l) throws IOException {
                byte[] chunk = off == 0 ? b : java.util.Arrays.copyOfRange(b, off, off + l);
                int n = conn.bulkTransfer(rOut, chunk, l, 5000);
                if (n != l) throw new IOException("USB write failed");
            }
            @Override public void write(int b) throws IOException { send(new byte[]{(byte) b}, 0, 1); }
            @Override public void write(byte[] b, int off, int l) throws IOException {
                if (l <= HEADER) { send(b, off, l); return; }
                send(b, off, HEADER);
                send(b, off + HEADER, l - HEADER);
            }
        };
    }

    @Override public void close() {
        try { conn.releaseInterface(intf); } catch (Exception ignored) {}
        conn.close();
    }
}
