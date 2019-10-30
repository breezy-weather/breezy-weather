package wangdaye.com.geometricweather.remoteviews.trend;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.widget.trend.chart.PolylineAndHistogramView;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Widget item view.
 * */
public class WidgetItemView extends ViewGroup {

    private PolylineAndHistogramView trend;
    private Paint paint;

    private float width;

    @Nullable private String titleText;
    @Nullable private String subtitleText;

    @Nullable private Drawable topIconDrawable;
    @Nullable private Drawable bottomIconDrawable;

    @ColorInt private int contentColor;
    @ColorInt private int subtitleColor;

    private float titleTextBaseLine;

    private float subtitleTextBaseLine;

    private float topIconLeft;
    private float topIconTop;

    private float trendViewTop;

    private float bottomIconLeft;
    private float bottomIconTop;

    private int iconSize;

    protected static final int ICON_SIZE_DIP = 32;
    protected static final int TREND_VIEW_HEIGHT_DIP_1X = 96;
    protected static final int TREND_VIEW_HEIGHT_DIP_2X = 108;

    protected static final int TEXT_MARGIN_DIP = 2;
    protected static final int ICON_MARGIN_DIP = 4;
    protected static final int MARGIN_VERTICAL_DIP = 8;

    public WidgetItemView(Context context) {
        super(context);
        this.initialize();
    }

    public WidgetItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public WidgetItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WidgetItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize();
    }

    private void initialize() {
        setWillNotDraw(false);

        trend = new PolylineAndHistogramView(getContext());
        addView(trend);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.widget_content_text_size));
        paint.setTextAlign(Paint.Align.CENTER);

        setColor(true);

        iconSize = (int) DisplayUtils.dpToPx(getContext(), ICON_SIZE_DIP);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float height = 0;

        float textMargin = DisplayUtils.dpToPx(getContext(), TEXT_MARGIN_DIP);
        float iconMargin = DisplayUtils.dpToPx(getContext(), ICON_MARGIN_DIP);

        Paint.FontMetrics fontMetrics = paint.getFontMetrics();

        // title text.
        if (titleText != null) {
            height += DisplayUtils.dpToPx(getContext(), MARGIN_VERTICAL_DIP);
            titleTextBaseLine = height - fontMetrics.top;
            height += fontMetrics.bottom - fontMetrics.top;
            height += textMargin;
        }

        // subtitle text.
        if (subtitleText != null) {
            height += textMargin;
            subtitleTextBaseLine = height - fontMetrics.top;
            height += fontMetrics.bottom - fontMetrics.top;
            height += textMargin;
        }

        // top icon.
        if (topIconDrawable != null) {
            height += iconMargin;
            topIconLeft = (width - iconSize) / 2f;
            topIconTop = height;
            height += iconSize;
            height += iconMargin;
        }

        // trend item view.
        trendViewTop = height;
        int trendViewHeight = bottomIconDrawable == null
                ? (int) DisplayUtils.dpToPx(getContext(), TREND_VIEW_HEIGHT_DIP_1X)
                : (int) DisplayUtils.dpToPx(getContext(), TREND_VIEW_HEIGHT_DIP_2X);
        trend.measure(
                MeasureSpec.makeMeasureSpec((int) width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(trendViewHeight, MeasureSpec.EXACTLY)
        );
        height += trend.getMeasuredHeight();

        // bottom icon.
        if (bottomIconDrawable != null) {
            height += iconMargin;
            bottomIconLeft = (width - iconSize) / 2f;
            bottomIconTop = height;
            height += iconSize;
        }

        // margin bottom.
        height += (int) (DisplayUtils.dpToPx(getContext(), MARGIN_VERTICAL_DIP));

        setMeasuredDimension((int) width, (int) height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        trend.layout(
                0,
                (int) trendViewTop,
                trend.getMeasuredWidth(),
                (int) (trendViewTop + trend.getMeasuredHeight())
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // week text.
        if (titleText != null) {
            paint.setColor(contentColor);
            canvas.drawText(titleText, getMeasuredWidth() / 2f, titleTextBaseLine, paint);
        }

        // date text.
        if (subtitleText != null) {
            paint.setColor(subtitleColor);
            canvas.drawText(subtitleText, getMeasuredWidth() / 2f, subtitleTextBaseLine, paint);
        }

        int restoreCount;

        // day icon.
        if (topIconDrawable != null) {
            restoreCount = canvas.save();
            canvas.translate(topIconLeft, topIconTop);
            topIconDrawable.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }

        // night icon.
        if (bottomIconDrawable != null) {
            restoreCount = canvas.save();
            canvas.translate(bottomIconLeft, bottomIconTop);
            bottomIconDrawable.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
    }

    // control.

    public void setColor(boolean daytime) {
        if (daytime) {
            contentColor = ContextCompat.getColor(getContext(), R.color.colorTextContent_light);
            subtitleColor = ContextCompat.getColor(getContext(), R.color.colorTextSubtitle_light);
        } else {
            contentColor = ContextCompat.getColor(getContext(), R.color.colorTextContent_dark);
            subtitleColor = ContextCompat.getColor(getContext(), R.color.colorTextSubtitle_dark);
        }
    }

    public void setSize(float width) {
        this.width = width;
    }

    public void setTitleText(@Nullable String titleText) {
        this.titleText = titleText;
    }

    public void setSubtitleText(@Nullable String subtitleText) {
        this.subtitleText = subtitleText;
    }

    public void setTopIconDrawable(@Nullable Drawable d) {
        topIconDrawable = d;
        if (d != null) {
            d.setVisible(true, true);
            d.setCallback(this);
            d.setBounds(0, 0, iconSize, iconSize);
        }
    }

    public void setBottomIconDrawable(@Nullable Drawable d) {
        bottomIconDrawable = d;
        if (d != null) {
            d.setVisible(true, true);
            d.setCallback(this);
            d.setBounds(0, 0, iconSize, iconSize);
        }
    }

    public PolylineAndHistogramView getTrendItemView() {
        return trend;
    }

    public int getIconSize() {
        return iconSize;
    }
}
