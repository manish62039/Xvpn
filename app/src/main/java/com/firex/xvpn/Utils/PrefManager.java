package com.firex.xvpn.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.firex.xvpn.Models.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PrefManager {
    private static PrefManager prefManager;
    private final SharedPreferences.Editor editor;
    private final SharedPreferences sharedPreferences;

    public PrefManager(Context c) {
        sharedPreferences = c.getSharedPreferences("XVPN", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public boolean getBoolean(String key, boolean def) {
        return sharedPreferences.getBoolean(key, def);
    }

    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value).apply();
    }

    public static PrefManager getInstance(Context c) {
        if (prefManager == null)
            prefManager = new PrefManager(c);

        return prefManager;
    }

    public int getInt(String key) {
        return sharedPreferences.getInt(key, -1);
    }

    public void putInt(String key, int value) {
        editor.putInt(key, value).apply();
    }

    public void addServer(String ip, String serverName) {
        editor.putLong(ip + "_usedTime", System.currentTimeMillis()).apply();
        editor.putString("lastServer", serverName).apply();
    }

    public String getLastServer() {
        return sharedPreferences.getString("lastServer", "japan70");
    }

    public long getLastUsed(String ip) {
        return sharedPreferences.getLong(ip + "_usedTime", 0L);
    }

    public String getServerName(Server server) {
        String serverName = sharedPreferences.getString(server.ipAddress + "_name", null);
        if (serverName != null)
            return serverName;

        serverName = server.getCountryLong();
        String[] words = serverName.split(" ");
        serverName = words[0];
        if (words.length > 1) {
            serverName += words[1];
        }

        int count = sharedPreferences.getInt(server.getCountryShort() + "_count", 0);
        editor.putInt(server.getCountryShort() + "_count", ++count).apply();
        serverName += count;
        editor.putString(server.ipAddress + "_name", serverName);

        return serverName;
    }

}
