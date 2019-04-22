package wangdaye.com.geometricweather.basic.model.about;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;

/**
 * About app library.
 * */

public class AboutAppLibrary {

    public String title;
    public String content;
    public String url;

    private AboutAppLibrary(String title, String content, String url) {
        this.title = title;
        this.content = content;
        this.url = url;
    }

    public static List<AboutAppLibrary> buildLibraryList(Context context) {
        List<AboutAppLibrary> list = new ArrayList<>();

        list.add(new AboutAppLibrary(
                context.getString(R.string.retrofit),
                context.getString(R.string.about_retrofit),
                "https://github.com/square/retrofit"
        ));
        list.add(new AboutAppLibrary(
                context.getString(R.string.glide),
                context.getString(R.string.about_glide),
                "https://github.com/bumptech/glide"
        ));
        list.add(new AboutAppLibrary(
                context.getString(R.string.greenDAO),
                context.getString(R.string.about_greenDAO),
                "https://github.com/greenrobot/greenDAO"
        ));
        list.add(new AboutAppLibrary(
                context.getString(R.string.page_indicator),
                context.getString(R.string.about_page_indicator),
                "https://github.com/DavidPacioianu/InkPageIndicator"
        ));
        list.add(new AboutAppLibrary(
                context.getString(R.string.circular_progress_view),
                context.getString(R.string.about_circular_progress_view),
                "https://github.com/rahatarmanahmed/CircularProgressView"
        ));

        return list;
    }
}
