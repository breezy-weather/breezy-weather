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

    private final GeoActivity mActivity;
    private final List<Object> mModelList;

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
        mActivity = activity;

        mModelList = new ArrayList<>();
        mModelList.add(1);
        mModelList.add(0);
        mModelList.add(activity.getString(R.string.about_app));
        mModelList.addAll(AboutAppLink.buildLinkList(activity));
        mModelList.add(0);
        mModelList.add(activity.getString(R.string.donate));
        mModelList.addAll(AboutAppLink.buildDonateLinkList(activity));
        mModelList.add(0);
        mModelList.add(activity.getString(R.string.translator));
        mModelList.addAll(AboutAppTranslator.buildTranslatorList());
        // modelList.add(0);
        // modelList.add(activity.getString(R.string.thanks));
        // modelList.addAll(AboutAppLibrary.buildLibraryList(activity));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Object model = mModelList.get(viewType);
        if (model instanceof Integer) {
            if (((Integer) model) == 1) {
                return new HeaderHolder(
                        mActivity,
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.item_about_header, parent, false));
            } else {
                return new ViewHolder(
                        mActivity,
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.item_about_line, parent, false));
            }
        } else if (model instanceof String) {
            return new TitleHolder(
                    mActivity,
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_about_title, parent, false));
        } else if (model instanceof AboutAppLink) {
            return new LinkHolder(
                    mActivity,
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_about_link, parent, false));
        } else if (model instanceof AboutAppTranslator) {
            return new TranslatorHolder(
                    mActivity,
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_about_translator, parent, false));
        } else {
            return new LibraryHolder(
                    mActivity,
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_about_library, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(mActivity, mModelList.get(position));
    }

    @Override
    public int getItemCount() {
        return mModelList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}

class HeaderHolder extends AboutAdapter.ViewHolder {

    private final AppCompatImageView mImage;

    HeaderHolder(GeoActivity activity, View itemView) {
        super(activity, itemView);
        mImage = itemView.findViewById(R.id.item_about_header_appIcon);
    }

    @Override
    void onBindView(GeoActivity activity, Object model) {
        ImageHelper.load(activity, mImage, R.drawable.ic_launcher);
    }
}

class TitleHolder extends AboutAdapter.ViewHolder {

    private final TextView mTitle;

    TitleHolder(GeoActivity activity, View itemView) {
        super(activity, itemView);
        mTitle = itemView.findViewById(R.id.item_about_title);
    }

    @Override
    void onBindView(GeoActivity activity, Object model) {
        mTitle.setText((String) model);
    }
}

class LinkHolder extends AboutAdapter.ViewHolder
        implements View.OnClickListener {

    private final AppCompatImageView mIcon;
    private final TextView mTitle;

    private String mUrl;
    private boolean mEmail;

    LinkHolder(GeoActivity activity, View itemView) {
        super(activity, itemView);
        itemView.findViewById(R.id.item_about_link).setOnClickListener(this);
        mIcon = itemView.findViewById(R.id.item_about_link_icon);
        mTitle = itemView.findViewById(R.id.item_about_link_text);
    }

    @Override
    void onBindView(GeoActivity activity, Object model) {
        AboutAppLink link = (AboutAppLink) model;
        mIcon.setImageResource(link.iconRes);
        mTitle.setText(link.title);
        mUrl = link.url;
        mEmail = link.email;
    }

    @Override
    public void onClick(View v) {
        if (mUrl.equals(AboutAppLink.LINK_ALIPAY)) {
            DonateHelper.donateByAlipay(activity);
        } else if (mUrl.equals(AboutAppLink.LINK_WECHAT)) {
            DonateHelper.donateByWechat(activity);
        } else if (mEmail) {
            IntentHelper.startWebViewActivity(activity, mUrl);
        } else {
            IntentHelper.startWebViewActivity(activity, mUrl);
        }
    }
}

class TranslatorHolder extends AboutAdapter.ViewHolder implements View.OnClickListener {

    private final TextView mTitle;
    private final TextView mContent;
    private final AppCompatImageView mFlag;

    TranslatorHolder(GeoActivity activity, View itemView) {
        super(activity, itemView);
        itemView.findViewById(R.id.item_about_translator).setOnClickListener(this);
        mTitle = itemView.findViewById(R.id.item_about_translator_title);
        mContent = itemView.findViewById(R.id.item_about_translator_subtitle);
        mFlag = itemView.findViewById(R.id.item_about_translator_flag);
    }

    @Override
    void onBindView(GeoActivity activity, Object model) {
        AboutAppTranslator translator = (AboutAppTranslator) model;
        mTitle.setText(translator.name);
        mContent.setText(translator.email);
        ImageHelper.load(activity, mFlag, translator.flagResId);
    }

    @Override
    public void onClick(View v) {
        if (isEmail(mContent.getText().toString())) {
            IntentHelper.startEmailActivity(
                    activity,
                    Uri.parse("mailto:" + mContent.getText()).toString()
            );
        } else {
            IntentHelper.startWebViewActivity(activity, mContent.getText().toString());
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

    private final TextView mTitle;
    private final TextView mContent;

    private String mUrl;

    LibraryHolder(GeoActivity activity, View itemView) {
        super(activity, itemView);
        itemView.findViewById(R.id.item_about_library).setOnClickListener(this);
        mTitle = itemView.findViewById(R.id.item_about_library_title);
        mContent = itemView.findViewById(R.id.item_about_library_content);
    }

    @Override
    void onBindView(GeoActivity activity, Object model) {
        AboutAppLibrary library = (AboutAppLibrary) model;
        mTitle.setText(library.title);
        mContent.setText(library.content);
        mUrl = library.url;
    }

    @Override
    public void onClick(View v) {
        IntentHelper.startWebViewActivity(activity, mUrl);
    }
}