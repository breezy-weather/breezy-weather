package wangdaye.com.geometricweather.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.about.AboutAppLibrary;
import wangdaye.com.geometricweather.data.entity.model.about.AboutAppLink;
import wangdaye.com.geometricweather.data.entity.model.about.AboutAppTranslator;
import wangdaye.com.geometricweather.utils.helpter.DonateHelper;

/**
 * About adapter.
 * */

public class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.ViewHolder> {

    private Context context;
    private List<Object> modelList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        
        Context c;

        ViewHolder(Context c, View itemView) {
            super(itemView);
            this.c = c;
        }

        void onBindView(Context context, Object model) {

        }
    }

    public AboutAdapter(Context context) {
        this.context = context;

        this.modelList = new ArrayList<>();
        modelList.add(1);
        modelList.add(0);
        modelList.add(context.getString(R.string.about_app));
        modelList.addAll(AboutAppLink.buildLinkList(context));
        modelList.add(0);
        modelList.add(context.getString(R.string.translator));
        modelList.addAll(AboutAppTranslator.buildTranslatorList());
        modelList.add(0);
        modelList.add(context.getString(R.string.thanks));
        modelList.addAll(AboutAppLibrary.buildLibraryList(context));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Object model = modelList.get(viewType);
        if (model instanceof Integer) {
            if (((Integer) model) == 1) {
                return new HeaderHolder(
                        context,
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_about_header, parent, false));
            } else {
                return new ViewHolder(
                        context,
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_about_line, parent, false));
            }
        } else if (model instanceof String) {
            return new TitleHolder(
                    context,
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_about_title, parent, false));
        } else if (model instanceof AboutAppLink) {
            return new LinkHolder(
                    context,
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_about_link, parent, false));
        } else if (model instanceof AboutAppTranslator) {
            return new TranslatorHolder(
                    context,
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_about_translator, parent, false));
        } else {
            return new LibraryHolder(
                    context,
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_about_library, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.onBindView(context, modelList.get(position));
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

    private ImageView image;

    HeaderHolder(Context context, View itemView) {
        super(context, itemView);
        this.image = (ImageView) itemView.findViewById(R.id.item_about_header_appIcon);
    }

    @Override
    void onBindView(Context context, Object model) {
        Glide.with(context)
                .load(R.drawable.ic_launcher)
                .into(image);
    }
}

class TitleHolder extends AboutAdapter.ViewHolder {

    private TextView title;

    TitleHolder(Context context, View itemView) {
        super(context, itemView);
        this.title = (TextView) itemView.findViewById(R.id.item_about_title);
    }

    @Override
    void onBindView(Context context, Object model) {
        title.setText((String) model);
    }
}

class LinkHolder extends AboutAdapter.ViewHolder
        implements View.OnClickListener {

    private ImageView icon;
    private TextView title;

    private String url;
    private boolean email;

    LinkHolder(Context context, View itemView) {
        super(context, itemView);
        itemView.findViewById(R.id.item_about_link).setOnClickListener(this);
        this.icon = (ImageView) itemView.findViewById(R.id.item_about_link_icon);
        this.title = (TextView) itemView.findViewById(R.id.item_about_link_text);
    }

    @Override
    void onBindView(Context context, Object model) {
        AboutAppLink link = (AboutAppLink) model;
        icon.setImageResource(link.iconRes);
        title.setText(link.title);
        url = link.url;
        email = link.email;
    }

    @Override
    public void onClick(View v) {
        if (TextUtils.isEmpty(url)) {
            // donate.
            DonateHelper.donateByAlipay(c);
        } else if (email) {
            c.startActivity(
                    new Intent(
                            Intent.ACTION_SENDTO,
                            Uri.parse(url)));
        } else {
            c.startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)));
        }
    }
}

class TranslatorHolder extends AboutAdapter.ViewHolder implements View.OnClickListener {

    private TextView title;
    private TextView content;
    private ImageView flag;

    TranslatorHolder(Context context, View itemView) {
        super(context, itemView);
        itemView.findViewById(R.id.item_about_translator).setOnClickListener(this);
        this.title = (TextView) itemView.findViewById(R.id.item_about_translator_title);
        this.content = (TextView) itemView.findViewById(R.id.item_about_translator_subtitle);
        this.flag = (ImageView) itemView.findViewById(R.id.item_about_translator_flag);
    }

    @Override
    void onBindView(Context context, Object model) {
        AboutAppTranslator translator = (AboutAppTranslator) model;
        title.setText(translator.name);
        content.setText(translator.email);
        Glide.with(context)
                .load(translator.flagResId)
                .into(flag);
    }

    @Override
    public void onClick(View v) {
        c.startActivity(
                new Intent(
                        Intent.ACTION_SENDTO,
                        Uri.parse("mailto:" + content.getText().toString())));
    }
}

class LibraryHolder extends AboutAdapter.ViewHolder implements View.OnClickListener {

    private TextView title;
    private TextView content;

    private String url;

    LibraryHolder(Context context, View itemView) {
        super(context, itemView);
        itemView.findViewById(R.id.item_about_library).setOnClickListener(this);
        this.title = (TextView) itemView.findViewById(R.id.item_about_library_title);
        this.content = (TextView) itemView.findViewById(R.id.item_about_library_content);
    }

    @Override
    void onBindView(Context context, Object model) {
        AboutAppLibrary library = (AboutAppLibrary) model;
        title.setText(library.title);
        content.setText(library.content);
        url = library.url;
    }

    @Override
    public void onClick(View v) {
        c.startActivity(
                new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url)));
    }
}