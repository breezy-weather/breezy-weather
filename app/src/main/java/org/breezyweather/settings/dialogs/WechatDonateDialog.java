package org.breezyweather.settings.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.breezyweather.R;
import org.breezyweather.common.utils.helpers.ImageHelperKt;

public class WechatDonateDialog {

    public static void show(Context context) {
        View view = LayoutInflater
                .from(context)
                .inflate(R.layout.dialog_donate_wechat, null, false);

        AppCompatImageView image = view.findViewById(R.id.dialog_donate_wechat_img);
        ImageHelperKt.load(image, R.drawable.donate_wechat);

        new MaterialAlertDialogBuilder(context)
                .setView(view)
                .show();
    }
}
