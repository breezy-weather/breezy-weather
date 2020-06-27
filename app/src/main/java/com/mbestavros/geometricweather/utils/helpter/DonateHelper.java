package com.mbestavros.geometricweather.utils.helpter;

import android.didikee.donate.AlipayDonate;
import android.didikee.donate.WeiXinDonate;

import com.mbestavros.geometricweather.basic.GeoActivity;
import com.mbestavros.geometricweather.ui.dialog.WechatDonateDialog;
import com.mbestavros.geometricweather.utils.SnackbarUtils;

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
