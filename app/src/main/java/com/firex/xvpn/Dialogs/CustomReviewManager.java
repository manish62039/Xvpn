package com.firex.xvpn.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firex.xvpn.BuildConfig;
import com.firex.xvpn.R;
import com.firex.xvpn.Utils.PrefManager;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

public class CustomReviewManager extends Dialog {
    private ReviewManager reviewManager;
    private final String TAG = "ReviewManager";
    private ReviewInfo reviewInf;
    private PrefManager prefManager;
    private RelativeLayout rateUsBtn;

    public CustomReviewManager(Context context) {
        super(context);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER_HORIZONTAL;
        getWindow().setAttributes(params);

        setTitle(null);
        setOnCancelListener(null);
        View view = LayoutInflater.from(context).inflate(R.layout.rating_dialog, null);

        rateUsBtn = view.findViewById(R.id.rate_btn);
        rateUsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                prefManager.putBoolean("isAlreadyRated", true);
                playStore(context);
            }
        });

        setContentView(view);
        prefManager = PrefManager.getInstance(context);

//        activateReviewInfo(context);
    }

    public void showIfNotRated() {
        int session = prefManager.getInt("rate_session");

        if (session < 0)
            session = 0;

        prefManager.putInt("rate_session", ++session);

        boolean isAlreadyRated = prefManager.getBoolean("isAlreadyRated", false);

        if (isAlreadyRated) {
            Log.i(TAG, "showIfNotRated: Already Rated!");
            return;
        }

        Log.i(TAG, "showIfNotRated: session: " + session);

        if (session % 2 == 0) {
            show();
        }

    }

//    private void activateReviewInfo(Context context) {
//        reviewManager = ReviewManagerFactory.create(context);
//        Task<ReviewInfo> managerInfoTask = reviewManager.requestReviewFlow();
//
//        long sentTime = System.currentTimeMillis();
//        managerInfoTask.addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                reviewInf = task.getResult();
//                Log.i(TAG, "activateReviewInfo: time taken: " + (System.currentTimeMillis() - sentTime));
//            } else {
//                Log.i(TAG, "activateReviewInfo: failed to start");
//            }
//        });
//    }

//    public void startReviewFlow(Context context) {
//        if (reviewInf != null) {
//            Task<Void> flow = reviewManager.launchReviewFlow((Activity) context, reviewInf);
//
//            flow.addOnCompleteListener(task -> {
//                prefManager.putBoolean("isAlreadyRated", true);
//
//                if (!task.isSuccessful()) {
//                    Log.i(TAG, "onComplete: failed to launch in app review!");
//                    playStore(context);
//                } else {
//                    Log.i(TAG, "onComplete: opened in app review!");
//                }
//            });
//
//        }
//    }

    public void playStore(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
        } catch (android.content.ActivityNotFoundException anfe) {
            Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show();
            Log.i("More Apps Error", anfe.getLocalizedMessage());
            anfe.printStackTrace();
        }
    }

}
