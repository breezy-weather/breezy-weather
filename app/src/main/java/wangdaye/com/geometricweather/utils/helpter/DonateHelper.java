package wangdaye.com.geometricweather.utils.helpter;

import android.didikee.donate.AlipayDonate;
import android.didikee.donate.WeiXinDonate;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.settings.dialog.WechatDonateDialog;
import wangdaye.com.geometricweather.utils.SnackbarUtils;

/**
 * Donate helper.
 * */

public class DonateHelper {

    public static void donateByAlipay(GeoActivity activity) {
        if (AlipayDonate.hasInstalledAlipayClient(activity)) {
            AlipayDonate.startAlipayClient(activity, "fkx02882gqdh6imokjddj2a");
        } else {
            SnackbarUtils.showSnackbar(activity, "Alipay is not installed.");
        }
    }

    public static void donateByWechat(GeoActivity activity) {
        if (WeiXinDonate.hasInstalledWeiXinClient(activity)) {
            new WechatDonateDialog().show(activity.getSupportFragmentManager(), null);
        } else {
            SnackbarUtils.showSnackbar(activity, "WeChat is not installed.");
        }
    }
}
