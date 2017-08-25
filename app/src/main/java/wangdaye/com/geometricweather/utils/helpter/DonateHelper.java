package wangdaye.com.geometricweather.utils.helpter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.net.URLEncoder;

import wangdaye.com.geometricweather.utils.SnackbarUtils;

/**
 * Donate helper.
 * */

public class DonateHelper {

    public static void donateByAlipay(Context c) {
        if (!isInstalledAlipay(c)) {
            SnackbarUtils.showSnackbar("Need to install Alipay");
        } else {
            String url = "https://qr.alipay.com/a6x003871ksdfhcaplh7iab";
            try {
                url = URLEncoder.encode(url, "utf-8");
            } catch (Exception ignored) {

            }
            try {
                final String alipayqr = "alipayqr://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=" + url;
                c.startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(alipayqr + "%3F_s%3Dweb-other&_t=" + System.currentTimeMillis())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isInstalledAlipay(Context c) {
        PackageInfo packageInfo;
        try {
            packageInfo = c.getPackageManager().getPackageInfo("com.eg.android.AlipayGphone", 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        return packageInfo != null;
    }
}
