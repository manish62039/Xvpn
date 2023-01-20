package com.firex.xvpn.Adapters;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firex.xvpn.Activities.StartServerActivity;
import com.firex.xvpn.Models.Server;
import com.firex.xvpn.R;
import com.firex.xvpn.Utils.PrefManager;

import java.util.ArrayList;
import java.util.Random;

public class ServersAdapter extends RecyclerView.Adapter<ServersAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Server> servers;
    private final String TAG = "HomeServersAdapterTAGG";
    private boolean isFromHome;
    private String connectedServer;
    private PrefManager prefManager;

    public ServersAdapter(Context context, ArrayList<Server> servers, boolean isFromHome) {
        this.context = context;
        this.servers = servers;
        this.isFromHome = isFromHome;
        prefManager = PrefManager.getInstance(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(context)
                        .inflate(R.layout.server_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Server s = servers.get(position);

        //Server Name
        String finalServerName = prefManager.getServerName(s);
        holder.txt_serverName.setText(finalServerName);

        //Ping
        int ping = s.getPing() / 2;
        int colorCode = context.getColor(R.color.ping_red);
        if (ping < 200)
            colorCode = context.getColor(R.color.ping_green);

        String p = ping + "ms";

        holder.txt_ping.setText(p);
        holder.txt_ping.setTextColor(colorCode);

        //Country Flag
        int countryFlag = getCountryFlag(s.countryLong);
        holder.ic_country.setImageResource(countryFlag);

        //Tag
        String tag = "Free";

        if (position == 0)
            tag = "Best";

        if (isFromHome) {
            if (position == 1 || position == 2)
                tag = "Good";
            else if (position > 2) {
                tag = "Last Used";
                if (prefManager.getLastUsed(s.getIpAddress()) == 0L)
                    tag = "good";
            }
        } else {
            if (position > 0 && position < 9)
                tag = "Good";
        }
        holder.txtTag.setText(tag);

        //Start Server
        holder.parent.setOnClickListener(view -> {
            Intent intent = new Intent(context, StartServerActivity.class);
            intent.putExtra("serverExtra", s);
            intent.putExtra("serverName", finalServerName);
            intent.putExtra("flagCode", countryFlag);
            context.startActivity(intent);
        });

        if (connectedServer != null && !connectedServer.isEmpty() && finalServerName.equals(connectedServer)) {
            holder.checkMark.setImageResource(R.drawable.check_mark_icon_selected);
            holder.txtTag.setText(context.getString(R.string.connected));
            holder.txtTag.setBackground(ContextCompat.getDrawable(context, R.drawable.item_tag_bg_connected));
        } else {
            holder.checkMark.setImageResource(R.drawable.check_mark_icon);
            holder.txtTag.setBackground(ContextCompat.getDrawable(context, R.drawable.item_tag_bg));
        }
    }

    public void setSelectedServer(String connectedServer) {
        this.connectedServer = connectedServer;
    }

    private int getCountryFlag(String country) {
        int image_res = R.drawable.japan;
        switch (country) {
            case "Russian Federation":
                image_res = R.drawable.russia;
                break;
            case "Viet Nam":
                image_res = R.drawable.vietnam;
                break;
            case "Singapore":
                image_res = R.drawable.singapore;
                break;
            case "United States":
                image_res = R.drawable.united_states;
                break;
            case "Ukraine":
                image_res = R.drawable.ukraine;
                break;
            case "Thailand":
                image_res = R.drawable.thailand;
                break;
            case "Korea Republic of":
                image_res = R.drawable.south_korea;
                break;
            case "India":
                image_res = R.drawable.india;
                break;
            case "Canada":
                image_res = R.drawable.canada;
                break;
            case "Guam":
                image_res = R.drawable.guam;
                break;
            case "Italy":
                image_res = R.drawable.italy;
                break;
            case "Australia":
                image_res = R.drawable.australia;
                break;
        }

        return image_res;
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ic_country;
        TextView txt_serverName;
        TextView txt_ping;
        RelativeLayout parent;
        TextView txtTag;
        ImageView checkMark;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ic_country = itemView.findViewById(R.id.img_country);
            txt_serverName = itemView.findViewById(R.id.txt_serverName);
            txt_ping = itemView.findViewById(R.id.txt_ping);
            parent = itemView.findViewById(R.id.RL_Parent);
            txtTag = itemView.findViewById(R.id.txt_tag);
            checkMark = itemView.findViewById(R.id.checkMarkIcon);
        }
    }

}
