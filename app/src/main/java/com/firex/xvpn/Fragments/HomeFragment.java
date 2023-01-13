package com.firex.xvpn.Fragments;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firex.xvpn.Activities.SelectServerActivity;
import com.firex.xvpn.Activities.SplashActivity;
import com.firex.xvpn.Activities.StartServerActivity;
import com.firex.xvpn.Adapters.ServersAdapter;
import com.firex.xvpn.Models.Server;
import com.firex.xvpn.Utils.PrefManager;
import com.firex.xvpn.Utils.ServersManager;
import com.firex.xvpn.Utils.UtilsMethods;
import com.firex.xvpn.databinding.FragmentHomeBinding;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    FragmentHomeBinding binding;
    Context context;
    ArrayList<Server> list;
    private BroadcastReceiver broadcastReceiver;
    private final String TAG = "HomeFragmentTAGG";
    private PrefManager prefManager;
    private boolean isChanged = false;
    private ServersAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        context = binding.getRoot().getContext();

        Log.i(TAG, "onCreateView: Starting Home Fragment!");
        prefManager = PrefManager.getInstance(context);

        binding.btnMoreServers.setOnClickListener(view -> {
            Intent intent = new Intent(context, SelectServerActivity.class);
            startActivity(intent);
        });

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

        return binding.getRoot();
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
                list = ServersManager.getInstance(context).getTop3Servers();
                Log.i(TAG, "run: list: " + list);
                if (list == null) {
                    restartApp();
                } else {
                    adapter = new ServersAdapter(context, list, true);
                    UtilsMethods.runOnUI(context, () -> binding.RVMain.setAdapter(adapter));
                }
            }
        }.start();

    }

    private void restartApp() {
        ServersManager.clearInstance();
        Intent i = new Intent(context, SplashActivity.class);
        context.startActivity(i);
        ((Activity) context).finish();
    }

    @Override
    public void onResume() {
        super.onResume();

        initRecyclerView();

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