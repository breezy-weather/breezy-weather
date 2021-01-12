package wangdaye.com.geometricweather.settings.adapter;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.settings.model.AboutAppLibrary;
import wangdaye.com.geometricweather.settings.model.AboutAppLink;
import wangdaye.com.geometricweather.settings.model.AboutAppTranslator;
import wangdaye.com.geometricweather.utils.helpter.DonateHelper;
import wangdaye.com.geometricweather.utils.helpter.ImageHelper;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

/**
 * About adapter.
 * */

public class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.ViewHolder> {

    private GeoActivity activity;
    private List<Object> modelList;

    static class ViewHolder extends RecyclerView.ViewHolder {

        GeoActivity activity;

        ViewHolder(GeoActivity activity, View itemView) {
            super(itemView);
            this.activity = activity;
        }

        void onBindView(GeoActivity activity, Object model) {

        }
    }

    public AboutAdapter(GeoActivity activity) {
        this.activity = activity;

        this.modelList = new ArrayList<>();
        modelList.add(1);
        modelList.add(0);
        modelList.add(activity.getString(R.string.about_app));
        modelList.addAll(AboutAppLink.buildLinkList(activity));
        modelList.add(activity.getString(R.string.donate));
        modelList.addAll(AboutAppLink.buildDonateLinkList(activity));
        modelList.add(0);
        modelList.add(activity.getString(R.string.translator));
        modelList.addAll(AboutAppTranslator.buildTranslatorList());
        modelList.add(0);
        modelList.add(activity.getString(R.string.thanks));
        modelList.addAll(AboutAppLibrary.buildLibraryList(activity));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Object model = modelList.get(viewType);
        if (model instanceof Integer) {
            if (((Integer) model) == 1) {
                return new HeaderHolder(
                        activity,
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.item_about_header, parent, false));
            } else {
                return new ViewHolder(
                        activity,
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.item_about_line, parent, false));
            }
        } else if (model instanceof String) {
            return new TitleHolder(
                    activity,
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_about_title, parent, false));
        } else if (model instanceof AboutAppLink) {
            return new LinkHolder(
                    activity,
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_about_link, parent, false));
        } else if (model instanceof AboutAppTranslator) {
            return new TranslatorHolder(
                    activity,
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_about_translator, parent, false));
        } else {
            return new LibraryHolder(
                    activity,
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_about_library, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(activity, modelList.get(position));
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}

class HeaderHolder extends AboutAdapter.ViewHolder {

    private AppCompatImageView image;

    HeaderHolder(GeoActivity activity, View itemView) {
        super(activity, itemView);
        this.image = itemView.findViewById(R.id.item_about_header_appIcon);
    }

    @Override
    void onBindView(GeoActivity activity, Object model) {
        ImageHelper.load(activity, image, R.drawable.ic_launcher);
    }
}

class TitleHolder extends AboutAdapter.ViewHolder {

    private TextView title;

    TitleHolder(GeoActivity activity, View itemView) {
        super(activity, itemView);
        this.title = itemView.findViewById(R.id.item_about_title);
    }

    @Override
    void onBindView(GeoActivity activity, Object model) {
        title.setText((String) model);
    }
}

class LinkHolder extends AboutAdapter.ViewHolder
        implements View.OnClickListener {

    private AppCompatImageView icon;
    private TextView title;

    private String url;
    private boolean email;

    LinkHolder(GeoActivity activity, View itemView) {
        super(activity, itemView);
        itemView.findViewById(R.id.item_about_link).setOnClickListener(this);
        this.icon = itemView.findViewById(R.id.item_about_link_icon);
        this.title = itemView.findViewById(R.id.item_about_link_text);
    }

    @Override
    void onBindView(GeoActivity activity, Object model) {
        AboutAppLink link = (AboutAppLink) model;
        icon.setImageResource(link.iconRes);
        title.setText(link.title);
        url = link.url;
        email = link.email;
    }

    @Override
    public void onClick(View v) {
        if (url.equals(AboutAppLink.LINK_ALIPAY)) {
            DonateHelper.donateByAlipay(activity);
        } else if (url.equals(AboutAppLink.LINK_WECHAT)) {
            DonateHelper.donateByWechat(activity);
        } else if (email) {
            IntentHelper.startWebViewActivity(activity, url);
        } else {
            IntentHelper.startWebViewActivity(activity, url);
        }
    }
}

class TranslatorHolder extends AboutAdapter.ViewHolder implements View.OnClickListener {

    private TextView title;
    private TextView content;
    private AppCompatImageView flag;

    TranslatorHolder(GeoActivity activity, View itemView) {
        super(activity, itemView);
        itemView.findViewById(R.id.item_about_translator).setOnClickListener(this);
        this.title = itemView.findViewById(R.id.item_about_translator_title);
        this.content = itemView.findViewById(R.id.item_about_translator_subtitle);
        this.flag = itemView.findViewById(R.id.item_about_translator_flag);
    }

    @Override
    void onBindView(GeoActivity activity, Object model) {
        AboutAppTranslator translator = (AboutAppTranslator) model;
        title.setText(translator.name);
        content.setText(translator.email);
        ImageHelper.load(activity, flag, translator.flagResId);
    }

    @Override
    public void onClick(View v) {
        if (isEmail(content.getText().toString())) {
            IntentHelper.startEmailActivity(
                    activity,
                    Uri.parse("mailto:" + content.getText()).toString()
            );
        } else {
            IntentHelper.startWebViewActivity(activity, content.getText().toString());
        }
    }

    private static boolean isEmail(String strEmail) {
        String strPattern = "^[a-zA-Z0-9][\\w.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z.]*[a-zA-Z]$";
        if (TextUtils.isEmpty(strPattern)) {
            return false;
        } else {
            return strEmail.matches(strPattern);
        }
    }
}

class LibraryHolder extends AboutAdapter.ViewHolder implements View.OnClickListener {

    private TextView title;
    private TextView content;

    private String url;

    LibraryHolder(GeoActivity activity, View itemView) {
        super(activity, itemView);
        itemView.findViewById(R.id.item_about_library).setOnClickListener(this);
        this.title = itemView.findViewById(R.id.item_about_library_title);
        this.content = itemView.findViewById(R.id.item_about_library_content);
    }

    @Override
    void onBindView(GeoActivity activity, Object model) {
        AboutAppLibrary library = (AboutAppLibrary) model;
        title.setText(library.title);
        content.setText(library.content);
        url = library.url;
    }

    @Override
    public void onClick(View v) {
        IntentHelper.startWebViewActivity(activity, url);
    }
}