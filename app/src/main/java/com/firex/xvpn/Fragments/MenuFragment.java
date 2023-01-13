package com.firex.xvpn.Fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.firex.xvpn.R;
import com.firex.xvpn.databinding.FragmentHomeBinding;
import com.firex.xvpn.databinding.FragmentMenuBinding;

public class MenuFragment extends Fragment {
    FragmentMenuBinding binding;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentMenuBinding.inflate(inflater, container, false);
        context = binding.getRoot().getContext();

        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

}