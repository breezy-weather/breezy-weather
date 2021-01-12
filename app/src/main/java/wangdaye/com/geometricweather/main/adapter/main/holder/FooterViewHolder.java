package wangdaye.com.geometricweather.main.adapter.main.holder;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.main.MainActivity;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

public class FooterViewHolder extends AbstractMainViewHolder {

    private TextView title;
    private Button editButton;

    public FooterViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.container_main_footer, parent, false));

        this.title = itemView.findViewById(R.id.container_main_footer_title);
        this.editButton = itemView.findViewById(R.id.container_main_footer_editButton);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(Context context, @NonNull Location location, @NonNull ResourceProvider provider,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled) {
        super.onBindView(context, location, provider, listAnimationEnabled, itemAnimationEnabled);

        float cardMarginsVertical = themeManager.getCardMarginsVertical(context);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
        if (cardMarginsVertical != 0) {
            params.setMargins(0, (int) -cardMarginsVertical, 0, 0);
        }
        itemView.setLayoutParams(params);

        title.setTextColor(themeManager.getHeaderTextColor(title.getContext()));
        title.setText("* Powered by " + location.getWeatherSource().getSourceUrl());

        editButton.setTextColor(themeManager.getHeaderTextColor(editButton.getContext()));
        editButton.setOnClickListener(v -> IntentHelper.startCardDisplayManageActivityForResult(
                (Activity) context,
                MainActivity.CARD_MANAGE_ACTIVITY
        ));
    }

    @NotNull
    @Override
    protected Animator getEnterAnimator(List<Animator> pendingAnimatorList) {
        Animator a = ObjectAnimator.ofFloat(itemView, "alpha", 0f, 1f);
        a.setDuration(450);
        a.setInterpolator(new FastOutSlowInInterpolator());
        a.setStartDelay(pendingAnimatorList.size() * 150);
        return a;
    }
}
