package wangdaye.com.geometricweather.data.entity.model.about;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;

/**
 * About app link.
 * */

public class AboutAppLink {

    public int iconRes;
    public String title;
    public String url;
    public boolean email;

    private AboutAppLink(int iconRes, String title, String url, boolean email) {
        this.iconRes = iconRes;
        this.title = title;
        this.url = url;
        this.email = email;
    }

    public static List<AboutAppLink> buildLinkList(Context context) {
        List<AboutAppLink> list = new ArrayList<>(3);

        list.add(new AboutAppLink(
                R.drawable.ic_github,
                context.getString(R.string.gitHub),
                "https://github.com/WangDaYeeeeee/GeometricWeather",
                false));
        list.add(new AboutAppLink(
                R.drawable.ic_email,
                context.getString(R.string.email),
                "mailto:wangdayeeeeee@gmail.com",
                true));
        list.add(new AboutAppLink(
                R.drawable.ic_donate,
                context.getString(R.string.donate),
                "",
                false));

        return list;
    }
}
