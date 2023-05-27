package wangdaye.com.geometricweather.settings.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper;
import wangdaye.com.geometricweather.common.utils.helpers.SnackbarHelper;
import wangdaye.com.geometricweather.settings.dialogs.WechatDonateDialog;

/**
 * TODO: It should be possible to donate with a webview
 * See here: https://global.alipay.com/docs/ac/agreementpayment/clientsideint
 */
public class DonateHelper {
    private static final String ALIPAY_PACKAGE_NAME = "com.eg.android.AlipayGphone";
    private static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";

    public static boolean isPackageInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();

        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            return info != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void donateByAlipay(GeoActivity activity) {
        if (DonateHelper.isPackageInstalled(activity, ALIPAY_PACKAGE_NAME)) {
            IntentHelper.startAlipayActivity(activity, "fkx02882gqdh6imokjddj2a");
        } else {
            SnackbarHelper.showSnackbar("Alipay is not installed.");
        }
    }

    public static void donateByWechat(GeoActivity activity) {
        if (DonateHelper.isPackageInstalled(activity, WECHAT_PACKAGE_NAME)) {
            WechatDonateDialog.show(activity);
        } else {
            SnackbarHelper.showSnackbar("WeChat is not installed.");
        }
    }
}
