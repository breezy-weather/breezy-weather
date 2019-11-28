package wangdaye.com.geometricweather.ui.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

import wangdaye.com.geometricweather.R;

public class WechatDonateDialog extends DialogFragment {

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_donate_wechat, null, false);

        AppCompatImageView image = view.findViewById(R.id.dialog_donate_wechat_img);
        Glide.with(getActivity())
                .load(R.drawable.donate_wechat)
                .into(image);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }
}
