package com.firex.xvpn.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.firex.xvpn.R;


public class HowToUseDialog extends Dialog {

    public HowToUseDialog(@NonNull Context context,String title,String text) {
        super(context);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER_HORIZONTAL;
        getWindow().setAttributes(params);

        setTitle(null);
        setOnCancelListener(null);
        View view = LayoutInflater.from(context).inflate(R.layout.update_dialog, null);

        RelativeLayout okButton = view.findViewById(R.id.ok_btn);
        TextView txtMsg = view.findViewById(R.id.msg_text_dialog);
        TextView txtTitle = view.findViewById(R.id.txt_title);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        txtMsg.setText(text);
        txtTitle.setText(title);

        setContentView(view);
    }

}