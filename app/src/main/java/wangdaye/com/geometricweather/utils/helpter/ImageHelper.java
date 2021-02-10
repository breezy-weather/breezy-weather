package wangdaye.com.geometricweather.utils.helpter;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;

public class ImageHelper {

    public static void load(Context context, ImageView target, @DrawableRes int resId) {
        Glide.with(context)
                .load(resId)
                .into(target);
    }
}
