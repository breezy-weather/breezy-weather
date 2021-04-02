package wangdaye.com.geometricweather.search.ui;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gordonwong.materialsheetfab.AnimatedFab;

public class FABView extends FloatingActionButton implements AnimatedFab {

    public FABView(@NonNull Context context) {
        super(context);
    }

    public FABView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FABView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void show(float translationX, float translationY) {
        super.show();
    }

    @Override
    public void hide() {
        super.hide();
    }
}