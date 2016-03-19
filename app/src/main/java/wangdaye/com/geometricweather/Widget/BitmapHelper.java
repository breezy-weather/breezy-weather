package wangdaye.com.geometricweather.Widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * A bitmap helper class.
 * */

public class BitmapHelper {
    public static Bitmap readBitMap(Context context, int resId, float width, float height){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, options);
        //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(context.getResources(), resId, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, float width, float height) {
        int originalWidth = options.outWidth;
        int originalHeight = options.outHeight;
        int inSampleSize = 1;

        if (originalWidth > width || originalHeight > height) {
            int halfWidth = originalWidth / 2;
            int halfHeight = originalHeight / 2;
            while ((halfWidth / inSampleSize > width) &&(halfHeight / inSampleSize > height)) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
