package wangdaye.com.geometricweather.ui.widget.trendView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
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
import androidx.core.content.ContextCompat;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.image.AbstractIconTarget;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Daily trend item view.
 * */
public class DailyItemView extends ViewGroup {

    private TrendItemView trend;
    private Paint paint;

    @Nullable private OnClickListener clickListener;

    @Nullable private String weekText;
    @Nullable private String dateText;

    @Nullable private Drawable dayIconDrawable;
    @Nullable private Drawable nightIconDrawable;

    @ColorInt private int contentColor;
    @ColorInt private int subTitleColor;

    private float weekTextBaseLine;

    private float dateTextBaseLine;

    private float dayIconLeft;
    private float dayIconTop;

    private float trendViewTop;

    private float nightIconLeft;
    private float nightIconTop;

    private int iconSize;

    private static final int ICON_SIZE_DIP = 32;
    private static final int TREND_VIEW_HEIGHT_DIP = 144;

    private static final int TEXT_MARGIN_DIP = 4;
    private static final int ICON_MARGIN_DIP = 8;
    private static final int MARGIN_BOTTOM_DIP = 16;

    public DailyItemView(Context context) {
        super(context);
        this.initialize();
    }

    public DailyItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public DailyItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DailyItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

        contentColor = ContextCompat.getColor(getContext(), R.color.colorTextContent);
        subTitleColor = ContextCompat.getColor(getContext(), R.color.colorTextSubtitle);

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
        weekTextBaseLine = height - fontMetrics.top;
        height += fontMetrics.bottom - fontMetrics.top;
        height += textMargin;

        // date text.
        height += textMargin;
        dateTextBaseLine = height - fontMetrics.top;
        height += fontMetrics.bottom - fontMetrics.top;
        height += textMargin;

        // day icon.
        height += iconMargin;
        dayIconLeft = (width - iconSize) / 2f;
        dayIconTop = height;
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

        // night icon.
        height += iconMargin;
        nightIconLeft = (width - iconSize) / 2f;
        nightIconTop = height;
        height += iconSize;
        height += iconMargin;

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
        if (weekText != null) {
            paint.setColor(contentColor);
            canvas.drawText(weekText, getMeasuredWidth() / 2f, weekTextBaseLine, paint);
        }

        // date text.
        if (dateText != null) {
            paint.setColor(subTitleColor);
            canvas.drawText(dateText, getMeasuredWidth() / 2f, dateTextBaseLine, paint);
        }

        int restoreCount;

        // day icon.
        if (dayIconDrawable != null) {
            restoreCount = canvas.save();
            canvas.translate(dayIconLeft, dayIconTop);
            dayIconDrawable.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }

        // night icon.
        if (nightIconDrawable != null) {
            restoreCount = canvas.save();
            canvas.translate(nightIconLeft, nightIconTop);
            nightIconDrawable.draw(canvas);
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

    public void setWeekText(String weekText) {
        this.weekText = weekText;
        invalidate();
    }

    public void setDateText(String dateText) {
        this.dateText = dateText;
        invalidate();
    }

    public void setDayIconDrawable(@Nullable Drawable d) {
        dayIconDrawable = d;
        if (d != null) {
            d.setVisible(true, true);
            d.setCallback(this);
            d.setBounds(0, 0, iconSize, iconSize);
        }
        invalidate();
    }

    public void setNightIconDrawable(@Nullable Drawable d) {
        nightIconDrawable = d;
        if (d != null) {
            d.setVisible(true, true);
            d.setCallback(this);
            d.setBounds(0, 0, iconSize, iconSize);
        }
        invalidate();
    }

    public AbstractIconTarget getDayIconTarget() {
        return new AbstractIconTarget(iconSize) {
            @Override
            public View getTarget() {
                return DailyItemView.this;
            }

            @Override
            public void setDrawableForTarget(Drawable d) {
                setDayIconDrawable(d);
            }

            @Override
            public Drawable getDrawableFromTarget() {
                return dayIconDrawable;
            }

            @Override
            public void setTagForTarget(Object tag) {
                setTag(R.id.tag_icon_day, tag);
            }

            @Override
            public Object getTagFromTarget() {
                return getTag(R.id.tag_icon_day);
            }
        };
    }

    public AbstractIconTarget getNightIconTarget() {
        return new AbstractIconTarget(iconSize) {
            @Override
            public View getTarget() {
                return DailyItemView.this;
            }

            @Override
            public void setDrawableForTarget(Drawable d) {
                setNightIconDrawable(d);
            }

            @Override
            public Drawable getDrawableFromTarget() {
                return nightIconDrawable;
            }

            @Override
            public void setTagForTarget(Object tag) {
                setTag(R.id.tag_icon_night, tag);
            }

            @Override
            public Object getTagFromTarget() {
                return getTag(R.id.tag_icon_night);
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

