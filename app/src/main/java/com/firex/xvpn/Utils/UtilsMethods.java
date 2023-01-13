package com.firex.xvpn.Utils;

import android.app.Activity;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Collections;

public class UtilsMethods {

    public static void runOnUI(Context c, Runnable r) {
        ((Activity) c).runOnUiThread(() -> ((Activity) c).runOnUiThread(r));
    }

    public static boolean isConnectedToInternet() {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();

            StrictMode.setThreadPolicy(policy);

            int timeoutMs = 1000;
            Socket sock = new Socket();
            SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);

            sock.connect(sockaddr, timeoutMs);
            sock.close();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean vpn() {
        String iface = "";
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.isUp())
                    iface = networkInterface.getName();
                Log.d("DEBUG", "IFACE NAME: " + iface);
                if (iface.contains("tun") || iface.contains("ppp") || iface.contains("pptp")) {
                    return true;
                }
            }
        } catch (SocketException e1) {
            e1.printStackTrace();
        }

        return false;
    }

}
