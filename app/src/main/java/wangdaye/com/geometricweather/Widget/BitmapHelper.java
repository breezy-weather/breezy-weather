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

    public static int[] getStarSize(int widthPixels) {
        if (widthPixels < 720) {
            return new int[] {200, 100};
        } else if (widthPixels < 1080) {
            return new int[] {300, 200};
        } else if (widthPixels == 1080) {
            return new int[] {400, 200};
        } else {
            return new int[] {600, 300};
        }
    }

    public static int getWeekIconSize(int widthPixels) {
        if (widthPixels < 720) {
            return 50;
        } else if (widthPixels < 1080) {
            return 65;
        } else if (widthPixels == 1080) {
            return 75;
        } else {
            return 100;
        }
    }

    public static int getTodayIconSize(int widthPixels) {
        if (widthPixels < 720) {
            return 70;
        } else if (widthPixels < 1080) {
            return 85;
        } else if (widthPixels == 1080) {
            return 100;
        } else {
            return 140;
        }
    }
}
