package com.firex.xvpn.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.firex.xvpn.Adapters.ServersAdapter;
import com.firex.xvpn.Models.Server;
import com.firex.xvpn.Utils.PrefManager;
import com.firex.xvpn.Utils.ServersManager;
import com.firex.xvpn.Utils.UtilsMethods;
import com.firex.xvpn.databinding.ActivitySelectServerBinding;

import java.util.ArrayList;

public class SelectServerActivity extends AppCompatActivity {
    ActivitySelectServerBinding binding;
    ArrayList<Server> list;
    private final String TAG = "SelectServerActivityTAG";
    private BroadcastReceiver broadcastReceiver;
    private Context context;
    private boolean isChanged;
    private PrefManager prefManager;
    private ServersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectServerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });
        context = this;

        initRecyclerView();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String state = intent.getStringExtra("state");
                Log.i(TAG, "onReceive: state: " + state);

                if (state != null) {
                    if (!state.equals("CONNECTED")) {
                        binding.statusHome.setVisibility(View.GONE);
                        adapter.setSelectedServer("");
                        adapter.notifyDataSetChanged();
                    }
                }

                try {
                    String duration = intent.getStringExtra("duration");

                    if (duration == null) duration = "00:00:00";

                    if (UtilsMethods.vpn() && !duration.equals("00:00:00"))
                        updateData(duration);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        prefManager = PrefManager.getInstance(context);
    }

    private void updateData(String duration) {
        binding.statusHome.setVisibility(View.VISIBLE);
        binding.txtStatusDuration.setText(duration);
        String serverName = "Connected to " + prefManager.getLastServer();
        binding.txtStatusServerName.setText(serverName);

        if (!isChanged) {
            isChanged = true;
            adapter.setSelectedServer(prefManager.getLastServer());
            adapter.notifyDataSetChanged();
        }
    }

    private void initRecyclerView() {
        new Thread() {
            @Override
            public void run() {
                if (list != null)
                    list.clear();
                list = ServersManager.getInstance(SelectServerActivity.this).getServers();
                if (list == null) {
                    restartApp();
                } else {
                    adapter = new ServersAdapter(SelectServerActivity.this, list, false);
                    Log.i(TAG, "initRecyclerView: ServersSize: " + list.size());

                    runOnUiThread(() -> binding.RVSelectServer.setAdapter(adapter));
                }
            }
        }.start();

    }

    private void restartApp() {
        ServersManager.clearInstance();
        Intent i = new Intent(SelectServerActivity.this, SplashActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter("connectionState"));
        binding.statusHome.setVisibility(View.GONE);
        isChanged = false;
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
        isChanged = false;
    }
}