package wangdaye.com.geometricweather.ui.widget.trendView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.image.AbstractIconTarget;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Daily trend item view.
 * */
public class HourlyItemView extends ViewGroup {

    private TrendItemView trend;
    private Paint paint;

    @Nullable private OnClickListener clickListener;

    @Nullable private String hourText;
    @Nullable private Drawable iconDrawable;

    @ColorInt private int contentColor;

    private float hourTextBaseLine;

    private float iconLeft;
    private float iconTop;

    private float trendViewTop;

    private int iconSize;

    private static final int ICON_SIZE_DIP = 32;
    private static final int TREND_VIEW_HEIGHT_DIP = 128;

    private static final int TEXT_MARGIN_DIP = 4;
    private static final int ICON_MARGIN_DIP = 8;
    private static final int MARGIN_BOTTOM_DIP = 16;

    public HourlyItemView(Context context) {
        super(context);
        this.initialize();
    }

    public HourlyItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public HourlyItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public HourlyItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize();
    }

    private void initialize() {
        setWillNotDraw(false);

        trend = new TrendItemView(getContext());
        addView(trend);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.content_text_size));
        paint.setTextAlign(Paint.Align.CENTER);

        setTextColor(Color.BLACK);

        iconSize = (int) DisplayUtils.dpToPx(getContext(), ICON_SIZE_DIP);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float width = (
                getResources().getDisplayMetrics().widthPixels
                        - 2 * getResources().getDimensionPixelSize(R.dimen.little_margin)
        ) / 5f;
        width = Math.max(DisplayUtils.dpToPx(getContext(), 56), width);

        float height = 0;

        float textMargin = DisplayUtils.dpToPx(getContext(), TEXT_MARGIN_DIP);
        float iconMargin = DisplayUtils.dpToPx(getContext(), ICON_MARGIN_DIP);

        Paint.FontMetrics fontMetrics = paint.getFontMetrics();

        // week text.
        height += textMargin;
        hourTextBaseLine = height - fontMetrics.top;
        height += fontMetrics.bottom - fontMetrics.top;
        height += textMargin;

        // day icon.
        height += iconMargin;
        iconLeft = (width - iconSize) / 2f;
        iconTop = height;
        height += iconSize;
        height += iconMargin;

        // trend item view.
        trendViewTop = height;
        trend.measure(
                MeasureSpec.makeMeasureSpec(
                        (int) width,
                        MeasureSpec.EXACTLY
                ), MeasureSpec.makeMeasureSpec(
                        (int) DisplayUtils.dpToPx(getContext(), TREND_VIEW_HEIGHT_DIP),
                        MeasureSpec.EXACTLY
                )
        );
        height += trend.getMeasuredHeight();

        // margin bottom.
        height += (int) (DisplayUtils.dpToPx(getContext(), MARGIN_BOTTOM_DIP));

        setMeasuredDimension((int) width, (int) height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        trend.layout(
                0,
                (int) trendViewTop,
                trend.getMeasuredWidth(),
                (int) trendViewTop + trend.getMeasuredHeight()
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // week text.
        if (hourText != null) {
            paint.setColor(contentColor);
            canvas.drawText(hourText, getMeasuredWidth() / 2f, hourTextBaseLine, paint);
        }

        // day icon.
        if (iconDrawable != null) {
            int restoreCount = canvas.save();
            canvas.translate(iconLeft, iconTop);
            iconDrawable.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (clickListener != null) {
                    clickListener.onClick(this);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setHourText(String hourText) {
        this.hourText = hourText;
        invalidate();
    }

    public void setTextColor(@ColorInt int contentColor) {
        this.contentColor = contentColor;
        invalidate();
    }

    public void setIconDrawable(@Nullable Drawable d) {
        iconDrawable = d;
        if (d != null) {
            d.setVisible(true, true);
            d.setCallback(this);
            d.setBounds(0, 0, iconSize, iconSize);
        }
        invalidate();
    }

    public AbstractIconTarget getIconTarget() {
        return new AbstractIconTarget(iconSize) {
            @Override
            public View getTarget() {
                return HourlyItemView.this;
            }

            @Override
            public void setDrawableForTarget(Drawable d) {
                setIconDrawable(d);
            }

            @Override
            public Drawable getDrawableFromTarget() {
                return iconDrawable;
            }

            @Override
            public void setTagForTarget(Object tag) {
                setTag(tag);
            }

            @Override
            public Object getTagFromTarget() {
                return getTag();
            }
        };
    }

    public TrendItemView getTrendItemView() {
        return trend;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        clickListener = l;
        super.setOnClickListener(v -> {});
    }
}

