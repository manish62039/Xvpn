package com.firex.xvpn.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;

import com.firex.xvpn.R;
import com.firex.xvpn.Utils.ServersManager;
import com.firex.xvpn.Utils.UtilsMethods;
import com.firex.xvpn.databinding.ActivitySplashBinding;
import com.google.android.material.snackbar.Snackbar;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    ActivitySplashBinding binding;
    ServersManager serversManager;
    private final String TAG = "SplashScreen";
    private Intent intent;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.i("CRASH", "onCreate: Starting Splash Activity!");
        startTime = System.currentTimeMillis();

        intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        binding.shimmerViewContainer.hideShimmer();

        Snackbar snackbar = Snackbar.make(binding.getRoot(), "Please Connect to Internet", Snackbar.LENGTH_INDEFINITE)
                .setBackgroundTint(SplashActivity.this.getColor(R.color.grey))
                .setTextColor(SplashActivity.this.getColor(R.color.red))
                .setAction("RETRY", view -> {

                })
                .setActionTextColor(SplashActivity.this.getColor(R.color.green));

        new Thread() {
            @Override
            public void run() {
                long sentTime = System.currentTimeMillis();
                boolean isConnected = UtilsMethods.isConnectedToInternet();
                Log.i(TAG, "run: checking connection: " + isConnected + ": " + (System.currentTimeMillis() - sentTime));
                while (!isConnected) {
                    Runnable runnable = () -> {
                        if (!snackbar.isShown())
                            snackbar.show();
                    };
                    UtilsMethods.runOnUI(SplashActivity.this, runnable);

                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isConnected = UtilsMethods.isConnectedToInternet();
                }

                Runnable runnable = snackbar::dismiss;
                UtilsMethods.runOnUI(SplashActivity.this, runnable);

                initServerManager();
            }
        }.start();


    }

    private void initServerManager() {
        Log.i("CRASH", "initServerManager: " + ServersManager.isAlreadyStarted());
        if (ServersManager.isAlreadyStarted()) {

            runOnUiThread(() -> new Handler().postDelayed(() -> {
                startActivity(intent);
                finish();
            }, 1000));

        } else {
            serversManager = ServersManager.getInstance(this);
            ServersManager.setPingingCompletedListener((count, size) -> {
                if (count == 10) {
                    binding.shimmerViewContainer.showShimmer(true);
                } else if (count == size - 10) {
                    binding.shimmerViewContainer.hideShimmer();
                } else if (count == size) {
                    Log.i("SplashScreen", "completedIn: " + (System.currentTimeMillis() - startTime));
                    ServersManager.removePingingCompletedListener();

                    startActivity(intent);
                    finish();
                }
            });
        }
    }

}