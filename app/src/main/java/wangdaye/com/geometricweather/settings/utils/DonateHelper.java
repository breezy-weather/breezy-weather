package wangdaye.com.geometricweather.settings.utils;

import android.didikee.donate.AlipayDonate;
import android.didikee.donate.WeiXinDonate;

import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.utils.helpers.SnackbarHelper;
import wangdaye.com.geometricweather.settings.dialogs.WechatDonateDialog;

public class DonateHelper {

    public static void donateByAlipay(GeoActivity activity) {
        if (AlipayDonate.hasInstalledAlipayClient(activity)) {
            AlipayDonate.startAlipayClient(activity, "fkx02882gqdh6imokjddj2a");
        } else {
            SnackbarHelper.showSnackbar("Alipay is not installed.");
        }
    }

    public static void donateByWechat(GeoActivity activity) {
        if (WeiXinDonate.hasInstalledWeiXinClient(activity)) {
            WechatDonateDialog.show(activity);
        } else {
            SnackbarHelper.showSnackbar("WeChat is not installed.");
        }
    }
}
