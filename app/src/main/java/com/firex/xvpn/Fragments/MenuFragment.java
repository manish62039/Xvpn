package com.firex.xvpn.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.firex.xvpn.Activities.PrivacyPolicyActivity;
import com.firex.xvpn.BuildConfig;
import com.firex.xvpn.Dialogs.HowToUseDialog;
import com.firex.xvpn.R;
import com.firex.xvpn.Utils.PrefManager;
import com.firex.xvpn.databinding.FragmentHomeBinding;
import com.firex.xvpn.databinding.FragmentMenuBinding;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MenuFragment extends Fragment {
    private static final String TAG = "MenuFragmentTAGG";
    FragmentMenuBinding binding;
    private Context context;
    private PrefManager prefManager;
    private ReviewInfo reviewInf;
    private ReviewManager reviewManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i("StartingFragment", "Menu");

        binding = FragmentMenuBinding.inflate(inflater, container, false);
        context = binding.getRoot().getContext();
        prefManager = PrefManager.getInstance(context);

        binding.MenuUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playStore(context);
            }
        });

        binding.MenuRateApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInAppReview();
            }
        });

        binding.MenuShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareApp(context);
            }
        });

        binding.MenuPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PrivacyPolicyActivity.class);
                startActivity(intent);
            }
        });

        binding.MenuHowToUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new HowToUseDialog(context, context.getString(R.string.how_to_use_title), context.getString(R.string.how_to_use_msg)).show();
            }
        });

        binding.MenuAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new HowToUseDialog(context, context.getString(R.string.About_title), context.getString(R.string.About_msg)).show();
            }
        });

        return binding.getRoot();
    }

    private void checkForUpdates() {
        String currentVer = BuildConfig.VERSION_NAME;

        try {
            DatabaseReference db = FirebaseDatabase.getInstance().getReference("playStoreVersion");
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() == null || snapshot.getValue().toString().isEmpty()) {
                        Log.i(TAG, "onDataChange: LatestVersion not found!");

                        binding.MenuUpdate.setVisibility(View.GONE);
                    } else if (!snapshot.getValue().toString().equals(currentVer)) {

                        Log.i(TAG, "onDataChange: A newer version found!");

                        binding.MenuUpdate.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.i(TAG, "onDataChange: LatestVersion not found!");
                    binding.MenuUpdate.setVisibility(View.GONE);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            binding.MenuUpdate.setVisibility(View.GONE);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        checkForUpdates();
        showRateMenu();
        activateReviewInfo();
    }

    public void playStore(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
        } catch (android.content.ActivityNotFoundException anfe) {
            Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show();
            Log.i("More Apps Error", anfe.getLocalizedMessage());
            anfe.printStackTrace();
        }
    }

    private void showRateMenu() {
        boolean isAlreadyRated = prefManager.getBoolean("isAlreadyRated", false);
        if (isAlreadyRated)
            binding.MenuRateApp.setVisibility(View.GONE);
        else
            binding.MenuRateApp.setVisibility(View.VISIBLE);
    }

    public void showInAppReview() {
        if (reviewInf != null)
            startReviewFlow();

    }

    private void activateReviewInfo() {
        reviewManager = ReviewManagerFactory.create(context);
        Task<ReviewInfo> managerInfoTask = reviewManager.requestReviewFlow();

        long sentTime = System.currentTimeMillis();
        managerInfoTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reviewInf = task.getResult();
                Log.i(TAG, "activateReviewInfo: time taken: " + (System.currentTimeMillis() - sentTime));
            } else {
                Log.i(TAG, "activateReviewInfo: failed to start");
            }
        });
    }

    public void startReviewFlow() {
        if (reviewInf != null) {
            Task<Void> flow = reviewManager.launchReviewFlow((Activity) context, reviewInf);

            flow.addOnCompleteListener(task -> {
                prefManager.putBoolean("isAlreadyRated", true);

                if (!task.isSuccessful()) {
                    Log.i(TAG, "onComplete: failed to launch in app review!");
                    playStore(context);
                } else {
                    Log.i(TAG, "onComplete: opened in app review!");
                }
            });

        }
    }

    public static void shareApp(Context context) {
        try {
            String msg = "Take a look at this Awesome VPN App ";
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "FireX VPN");
            String shareMessage = "\n" + msg + "\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            context.startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}