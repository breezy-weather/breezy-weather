package wangdaye.com.geometricweather.utils.helpter;

import android.content.Context;

import com.tencent.bugly.crashreport.CrashReport;

public class BuglyHelper {

    public static void init(Context context) {
        CrashReport.initCrashReport(context.getApplicationContext(), "148f1437d5", false);
    }

    public static void report(Exception e) {
        CrashReport.postCatchedException(e);
    }
}
