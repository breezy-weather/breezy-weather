package wangdaye.com.geometricweather.remoteviews.trend;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.ui.widgets.trend.chart.PolylineAndHistogramView;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

/**
 * Widget item view.
 * */
public class WidgetItemView extends ViewGroup {

    private PolylineAndHistogramView mTrend;
    private Paint mTitleTextPaint;
    private Paint mSubtitleTextPaint;

    private float mWidth;

    @Nullable private String mTitleText;
    @Nullable private String mSubtitleText;

    @Nullable private Drawable mTopIconDrawable;
    @Nullable private Drawable mBottomIconDrawable;

    @ColorInt private int mContentColor;
    @ColorInt private int mSubtitleColor;

    private float mTitleTextBaseLine;

    private float mSubtitleTextBaseLine;

    private float mTopIconLeft;
    private float mTopIconTop;

    private float mTrendViewTop;

    private float mBottomIconLeft;
    private float mBottomIconTop;

    private int mIconSize;

    protected static final int ICON_SIZE_DIP = 32;
    protected static final int TREND_VIEW_HEIGHT_DIP_1X = 96;
    protected static final int TREND_VIEW_HEIGHT_DIP_2X = 108;

    protected static final int TEXT_MARGIN_DIP = 2;
    protected static final int ICON_MARGIN_DIP = 4;
    protected static final int MARGIN_VERTICAL_DIP = 8;

    public WidgetItemView(Context context) {
        super(context);
        initialize();
    }

    public WidgetItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public WidgetItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public WidgetItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        setWillNotDraw(false);

        mTrend = new PolylineAndHistogramView(getContext());
        addView(mTrend);

        mTitleTextPaint = new Paint();
        mTitleTextPaint.setAntiAlias(true);
        mTitleTextPaint.setTextAlign(Paint.Align.CENTER);
        mTitleTextPaint.setTypeface(
                DisplayUtils.getTypefaceFromTextAppearance(getContext(), R.style.title_text)
        );
        mTitleTextPaint.setTextSize(
                getContext().getResources().getDimensionPixelSize(R.dimen.title_text_size)
        );

        mSubtitleTextPaint = new Paint();
        mSubtitleTextPaint.setAntiAlias(true);
        mSubtitleTextPaint.setTextAlign(Paint.Align.CENTER);
        mSubtitleTextPaint.setTypeface(
                DisplayUtils.getTypefaceFromTextAppearance(getContext(), R.style.content_text)
        );
        mSubtitleTextPaint.setTextSize(
                getContext().getResources().getDimensionPixelSize(R.dimen.content_text_size)
        );

        setColor(true);

        mIconSize = (int) DisplayUtils.dpToPx(getContext(), ICON_SIZE_DIP);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float height = 0;

        float textMargin = DisplayUtils.dpToPx(getContext(), TEXT_MARGIN_DIP);
        float iconMargin = DisplayUtils.dpToPx(getContext(), ICON_MARGIN_DIP);

        // title text.
        if (mTitleText != null) {
            Paint.FontMetrics fontMetrics = mTitleTextPaint.getFontMetrics();
            height += DisplayUtils.dpToPx(getContext(), MARGIN_VERTICAL_DIP);
            mTitleTextBaseLine = height - fontMetrics.top;
            height += fontMetrics.bottom - fontMetrics.top;
            height += textMargin;
        }

        // subtitle text.
        if (mSubtitleText != null) {
            Paint.FontMetrics fontMetrics = mSubtitleTextPaint.getFontMetrics();
            height += textMargin;
            mSubtitleTextBaseLine = height - fontMetrics.top;
            height += fontMetrics.bottom - fontMetrics.top;
            height += textMargin;
        }

        // top icon.
        if (mTopIconDrawable != null) {
            height += iconMargin;
            mTopIconLeft = (mWidth - mIconSize) / 2f;
            mTopIconTop = height;
            height += mIconSize;
            height += iconMargin;
        }

        // trend item view.
        mTrendViewTop = height;
        int trendViewHeight = mBottomIconDrawable == null
                ? (int) DisplayUtils.dpToPx(getContext(), TREND_VIEW_HEIGHT_DIP_1X)
                : (int) DisplayUtils.dpToPx(getContext(), TREND_VIEW_HEIGHT_DIP_2X);
        mTrend.measure(
                MeasureSpec.makeMeasureSpec((int) mWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(trendViewHeight, MeasureSpec.EXACTLY)
        );
        height += mTrend.getMeasuredHeight();

        // bottom icon.
        if (mBottomIconDrawable != null) {
            height += iconMargin;
            mBottomIconLeft = (mWidth - mIconSize) / 2f;
            mBottomIconTop = height;
            height += mIconSize;
        }

        // margin bottom.
        height += (int) (DisplayUtils.dpToPx(getContext(), MARGIN_VERTICAL_DIP));

        setMeasuredDimension((int) mWidth, (int) height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mTrend.layout(
                0,
                (int) mTrendViewTop,
                mTrend.getMeasuredWidth(),
                (int) (mTrendViewTop + mTrend.getMeasuredHeight())
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // week text.
        if (mTitleText != null) {
            mTitleTextPaint.setColor(mContentColor);
            canvas.drawText(mTitleText, getMeasuredWidth() / 2f, mTitleTextBaseLine, mTitleTextPaint);
        }

        // date text.
        if (mSubtitleText != null) {
            mSubtitleTextPaint.setColor(mSubtitleColor);
            canvas.drawText(mSubtitleText, getMeasuredWidth() / 2f, mSubtitleTextBaseLine, mSubtitleTextPaint);
        }

        int restoreCount;

        // day icon.
        if (mTopIconDrawable != null) {
            restoreCount = canvas.save();
            canvas.translate(mTopIconLeft, mTopIconTop);
            mTopIconDrawable.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }

        // night icon.
        if (mBottomIconDrawable != null) {
            restoreCount = canvas.save();
            canvas.translate(mBottomIconLeft, mBottomIconTop);
            mBottomIconDrawable.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
    }

    // control.

    public void setColor(boolean daytime) {
        if (daytime) {
            mContentColor = ContextCompat.getColor(getContext(), R.color.colorTextDark2nd);
            mSubtitleColor = ContextCompat.getColor(getContext(), R.color.colorTextGrey2nd);
        } else {
            mContentColor = ContextCompat.getColor(getContext(), R.color.colorTextLight2nd);
            mSubtitleColor = ContextCompat.getColor(getContext(), R.color.colorTextGrey);
        }
    }

    public void setSize(float width) {
        mWidth = width;
    }

    public void setTitleText(@Nullable String titleText) {
        mTitleText = titleText;
    }

    public void setSubtitleText(@Nullable String subtitleText) {
        mSubtitleText = subtitleText;
    }

    public void setTopIconDrawable(@Nullable Drawable d) {
        mTopIconDrawable = d;
        if (d != null) {
            d.setVisible(true, true);
            d.setCallback(this);
            d.setBounds(0, 0, mIconSize, mIconSize);
        }
    }

    public void setBottomIconDrawable(@Nullable Drawable d) {
        mBottomIconDrawable = d;
        if (d != null) {
            d.setVisible(true, true);
            d.setCallback(this);
            d.setBounds(0, 0, mIconSize, mIconSize);
        }
    }

    public PolylineAndHistogramView getTrendItemView() {
        return mTrend;
    }

    public int getIconSize() {
        return mIconSize;
    }
}
