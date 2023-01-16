package com.firex.xvpn.Utils;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.firex.xvpn.Models.Server;
import com.firex.xvpn.R;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.ToLongFunction;

public class ServersManager {
    private static ServersManager instance;
    private final String TAG = "Filter_ServersManager";
    private List<Server> servers;
    private final InputStream txtFile;
    private final PrefManager prefManager;
    private static PingCompleteListner pingingCompletedListener;
    private final int TIME_OUT_SERVER = 1000;

    public ServersManager(Context context) {
        Log.i(TAG, "ServersManager: StartingServerManager");
        prefManager = PrefManager.getInstance(context);
        txtFile = context.getResources().openRawResource(R.raw.servers_list);

        new Thread() {
            @Override
            public void run() {
                startServerManager(context);
            }
        }.start();

    }

    public static void clearInstance() {
        instance = null;
        pingingCompletedListener = null;
    }

    public ArrayList<Server> getTop3Servers() {
        Log.i(TAG, "getTop3Servers: serversExists:" + (servers != null));
        if (servers == null) {
            return null;

        }
        ArrayList<Server> list = new ArrayList<>(servers);
        sortServersByPing(list);

        ArrayList<Server> result = new ArrayList<>();
        if (list.size() > 10) {
            for (int i = 0; i < list.size(); i++) {
                if (result.size() > 2)
                    break;

                Server s = list.get(i);
                if (s.getCountryShort().equals("VN") || s.getCountryShort().equals("KR"))
                    continue;

                result.add(s);
            }

            ArrayList<Server> usedServers = sortServerByUse(list);

            for (int i = 0; i < usedServers.size(); i++) {
                Server server = usedServers.get(i);

                if (result.size() > 5)
                    break;

                if (!result.contains(server))
                    result.add(server);
            }

        }

        return result;
    }

    public ArrayList<Server> getServers() {
        Log.i(TAG, "getTop3Servers: serversExists:" + (servers != null));
        if (servers == null) {
            return null;
        }
        ArrayList<Server> list = new ArrayList<>(servers);
        sortServersByPing(list);

        ArrayList<Server> result = new ArrayList<>();
        for (Server s : list) {
            if (s.getCountryShort().equals("VN") || s.getCountryShort().equals("KR"))
                continue;
            if (s.getPing() < TIME_OUT_SERVER - 2)
                result.add(s);
        }

        for (Server s : list) {
            if (s.getPing() < TIME_OUT_SERVER - 2 && !result.contains(s))
                result.add(s);
        }

        return result;
    }

    private void startServerManager(Context c) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(txtFile));
            servers = CsvParser.parse(bufferedReader);
            loadLastPing();
            sortServersByPing(servers);

//            int count = 0;
//            int size = servers.size();
//            for (Server s : servers) {
//                Log.i(TAG, "lastSaved: Ping: " + (++count) + "/" + size + ": " + s.getPing() + "ms");
//            }

            startPinging(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLastPing() {
//        Set<String> countries = new HashSet<>();
        for (Server s : servers) {
            int ping = prefManager.getInt(s.getIpAddress());
            if (ping != -1)
                s.setPing(ping);
//            countries.add(s.getCountryLong());
        }

//        Log.i(TAG, "loadLastPing: countries: " + countries);
    }

    private void startPinging(Context c) {
        int size = servers.size();
        final int[] count = {0};
        Random random = new Random();

        for (int i = 0; i < size; i++) {
            int sleepTime = random.nextInt(TIME_OUT_SERVER * 1) + 10;
            int finalI = i;

            new Thread() {
                @Override
                public void run() {
                    try {
                        sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Server s = servers.get(finalI);
                    int ping = pingServer(s.getIpAddress(), s.getPort(), TIME_OUT_SERVER);
                    prefManager.putInt(s.getIpAddress(), ping);
                    s.setPing(ping);
//                    Log.i(TAG, "startPinging: Ping: " + (finalI) + "/" + size + ": " + ping + "ms");
                    if (pingingCompletedListener != null) {
                        Runnable r = () -> pingingCompletedListener.onComplete(++count[0], size);
                        UtilsMethods.runOnUI(c, r);
                    }
                }
            }.start();

        }
    }

    public static void setPingingCompletedListener(PingCompleteListner listener) {
        pingingCompletedListener = listener;
    }

    public static void removePingingCompletedListener() {
        pingingCompletedListener = null;
    }

    private void sortServersByPing(List<Server> list) {
        Collections.sort(list, new Comparator<Server>() {
            public int compare(Server o1, Server o2) {
                // compare two instance of `Score` and return `int` as result.
                return o1.getPing() - (o2.getPing());
            }
        });
    }

    private ArrayList<Server> sortServerByUse(List<Server> list) {
        ArrayList<Server> usedServers = new ArrayList<>();
        for (Server s : list) {
            if (prefManager.getLastUsed(s.getIpAddress()) != 0) {
                usedServers.add(s);
            }
        }

        Collections.sort(usedServers, new Comparator<Server>() {
            @Override
            public int compare(Server t1, Server t2) {
                return (int) (prefManager.getLastUsed(t2.getIpAddress()) - prefManager.getLastUsed(t1.getIpAddress()));
            }
        });

        for (Server s : list) {
            if (usedServers.size() > 5)
                return usedServers;
            else if (!usedServers.contains(s))
                usedServers.add(s);
        }

        return usedServers;
    }

//    private int pingServer(String server, int timeOut) {
//        long time = System.currentTimeMillis();
//        try {
//            InetAddress address = InetAddress.getByName(server);
//            boolean reachable = address.isReachable(timeOut);
//
//            if (reachable)
//                return (int) (System.currentTimeMillis() - time);
//        } catch (Exception e) {
//            Log.i(TAG, "Is host reachable? error: " + e.getLocalizedMessage());
//        }
//        return timeOut - 1;
//    }
//

    private int pingServer(String server, int port, int timeOut) {
        long sentTime = System.currentTimeMillis();
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();

            StrictMode.setThreadPolicy(policy);

            Socket sock = new Socket();
            SocketAddress sockaddr = new InetSocketAddress(server, port);

            sock.connect(sockaddr, timeOut);
            sock.close();

            return (int) (System.currentTimeMillis() - sentTime);
        } catch (Exception e) {
            return timeOut - 1;
        }
    }

//    private boolean isConnectedToInternet() {
//        try {
//            URL url = new URL("https://www.google.com/");
//
//            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
//            urlc.setRequestProperty("User-Agent", "Android Application:" + "1.0.0");
//            urlc.setRequestProperty("Connection", "close");
//            urlc.setConnectTimeout(1000 * 2); // mTimeout is in seconds
//            urlc.connect();
//
//            if (urlc.getResponseCode() == 200) {
//                return true;
//            }
//        } catch (IOException e1) {
//            Log.i(TAG, "isConnectedToInternet: error: " + e1.getLocalizedMessage());
//            e1.printStackTrace();
//        }
//        return false;
//    }

//    private boolean isConnectedToInternet() {
//        try {
//            InetAddress address = InetAddress.getByName("https://www.google.com/");
//            return address.isReachable(2000);
//        } catch (Exception e) {
//            Log.i(TAG, "Is host reachable? error: " + e.getLocalizedMessage());
//            e.printStackTrace();
//        }
//        return false;
//    }

    public static ServersManager getInstance(Context context) {
        if (instance == null)
            instance = new ServersManager(context);

        return instance;
    }

    public static boolean isAlreadyStarted() {
        return (instance != null);
    }

    public interface PingCompleteListner {
        void onComplete(int count, int size);
    }
}
