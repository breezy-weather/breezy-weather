package wangdaye.com.geometricweather.search.ui;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gordonwong.materialsheetfab.AnimatedFab;

public class FabView extends FloatingActionButton implements AnimatedFab {

    public FabView(@NonNull Context context) {
        this(context, null);
    }

    public FabView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FabView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void show(float translationX, float translationY) {
        setTranslationX(translationX);
        setTranslationY(translationY);
        super.show();
    }

    @Override
    public void hide() {
        super.hide();
    }
}