package com.firex.xvpn.Activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.firex.xvpn.Models.Server;

import com.firex.xvpn.R;
import com.firex.xvpn.Utils.PrefManager;
import com.firex.xvpn.Utils.UtilsMethods;
import com.firex.xvpn.databinding.ActivityStartServerBinding;
import com.google.android.material.snackbar.Snackbar;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.OpenVPNThread;


public class StartServerActivity extends AppCompatActivity {
    ActivityStartServerBinding binding;
    private final String TAG = "StartServerActivityTAGG";
    private Server server;
    private Context context;
    private BroadcastReceiver broadcastReceiver;
    private String serverName;
    private boolean isProcessShowing = false;
    private boolean vpnStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStartServerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        context = this;
        Intent intent = getIntent();
        server = intent.getParcelableExtra("serverExtra");

        Intent i = new Intent(context, SplashActivity.class);

        if (server == null) {
            startActivity(i);
            finishActivity();
        }

        binding.imgCountry.setImageResource(intent.getIntExtra("flagCode", R.drawable.japan));
        serverName = intent.getStringExtra("serverName");
        setConnecting(true);
        hideProgress(false);

        if (UtilsMethods.vpn())
            stopVpn();
        prepareVpn();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String state = intent.getStringExtra("state");

                if (state != null) {
                    Log.i(TAG, "onReceive: state: " + state);

                    if (state.equals("CONNECTED")) {
                        setConnected();
                    }
                }

                try {
                    String duration = intent.getStringExtra("duration");
                    String byteIn = intent.getStringExtra("byteIn");
                    String byteOut = intent.getStringExtra("byteOut");

                    if (duration == null) duration = "00:00:00";
                    if (byteIn == null) byteIn = "0.0";
                    if (byteOut == null) byteOut = "0.0";

                    updateData(duration, byteIn, byteOut);
                    if (UtilsMethods.vpn() && !duration.equals("00:00:00"))
                        setConnected();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        binding.btnStop.setOnClickListener(view -> finishActivity());
    }

    @Override
    public void onBackPressed() {
        if (isProcessShowing) {
            stopVpn();
        }
        super.onBackPressed();
        finish();
    }

    private void setConnected() {
        if (isProcessShowing) {
            vpnStart = true;
            hideProgress(true);

            binding.btnStop.setImageResource(R.drawable.btn_disconnect);
            setConnecting(false);

            PrefManager prefManager = PrefManager.getInstance(context);
            prefManager.addServer(server.getIpAddress(), serverName);

        }
    }

    private void updateData(String duration, String bytesIn, String bytesOut) {
        binding.txtTimer.setText(duration);

        binding.txtDownloadValue.setText(getTargetData(bytesIn));
        binding.txtUploadValue.setText(getTargetData(bytesOut));

    }

    private String getTargetData(String data) {
        if (data == null || !data.contains("-"))
            return data;

        int indexOfMinus = data.indexOf("-");
        return data.substring(1, indexOfMinus).toUpperCase();
    }

    private void showProgress() {
        isProcessShowing = true;
        binding.shimmerViewContainer.showShimmer(true);
        binding.ShimmerData.showShimmer(true);
        binding.shimmerTimer.showShimmer(true);
    }

    private void hideProgress(boolean isConnected) {
        isProcessShowing = false;
        binding.shimmerViewContainer.hideShimmer();
        binding.ShimmerData.hideShimmer();
        binding.shimmerTimer.hideShimmer();

        if (isConnected) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(100);
        }
    }

    private void setConnecting(boolean isConnecting) {
        String msg = "Connecting to " + serverName;
        if (!isConnecting) {
            msg = "Connected to " + serverName;
        }
        binding.txtServerName.setText(msg);
    }

    private void finishActivity() {
        stopVpn();
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter("connectionState"));

        if (vpnStart) {
            if (!UtilsMethods.vpn()) {
                vpnStart = false;
                prepareVpn();
            }
        }

    }


    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
    }

    private void prepareVpn() {
        binding.btnStop.setImageResource(R.drawable.btn_stop);
        Snackbar snackbar = Snackbar.make(binding.getRoot(), "Please Connect to Internet", Snackbar.LENGTH_INDEFINITE)
                .setBackgroundTint(context.getColor(R.color.grey))
                .setTextColor(context.getColor(R.color.red))
                .setAction("RETRY", view -> {

                })
                .setActionTextColor(context.getColor(R.color.green));

        new Thread() {
            @Override
            public void run() {
                if (!vpnStart) {
                    while (!UtilsMethods.isConnectedToInternet()) {
                        runOnUiThread(() -> {
                            if (!snackbar.isShown())
                                snackbar.show();
                        });

                        try {
                            //noinspection BusyWait
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    runOnUiThread(() -> {
                        snackbar.dismiss();

                        Intent intent = VpnService.prepare(StartServerActivity.this);
                        if (intent != null) {
                            someActivityResultLauncher.launch(intent);
                        } else {
                            startVpn();
                        }
                    });

                    Log.i(TAG, "run: Connecting to vpn");

                }
            }
        }.start();

    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //Permission granted, start the VPN
                        startVpn();
                    } else {
                        Toast.makeText(context, "For a successful VPN connection, permission must be granted!", Toast.LENGTH_LONG).show();

                        new Handler().postDelayed(() -> ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).clearApplicationUserData(), 2000);
                    }
                }
            });

    private void startVpn() {
        vpnStart = false;
        showProgress();
        setConnecting(true);
        updateData("00:00:00", "0.0", "0.0");

        try {
            String conf = server.getOvpnConfigData();
            OpenVpnApi.startVpn(context, conf, server.getCountryShort(), "vpn", "vpn");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopVpn() {
        try {
            OpenVPNThread.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}