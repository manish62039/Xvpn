package com.firex.xvpn.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.firex.xvpn.Fragments.HomeFragment;
import com.firex.xvpn.Fragments.MenuFragment;
import com.firex.xvpn.R;
import com.firex.xvpn.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    Fragment[] fragments = new Fragment[2];
    int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fragments[0] = new HomeFragment();
        fragments[1] = new MenuFragment();

        binding.btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFragment(0);
            }
        });

        binding.btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFragment(1);
            }
        });

        selectFragment(0);
    }

    private void selectFragment(int index) {
        if (currentIndex == index)
            return;

        currentIndex = index;

        changeFragment(this, fragments[index], binding.fragmentContainer.getId());

        if (index == 0) {
            binding.btnHome.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.bottom_nav_selected_bg));
            binding.btnMenu.setBackground(null);
        } else {
            binding.btnMenu.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.bottom_nav_selected_bg));
            binding.btnHome.setBackground(null);
        }
    }

    private void changeFragment(FragmentActivity fragmentActivity, Fragment fragment, int continer_Id) {
        FragmentTransaction fragmentTransaction =
                fragmentActivity.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(continer_Id, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (currentIndex == 1) {
            selectFragment(0);
            return;
        }

        super.onBackPressed();
    }
}