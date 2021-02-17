package wangdaye.com.geometricweather.ui.widgets.trend;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.widgets.trend.item.AbsTrendItemView;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Trend recycler view.
 * */

public class TrendRecyclerView extends RecyclerView {

    private Paint mPaint;
    @ColorInt private int mLineColor;

    private int mDrawingBoundaryTop;
    private int mDrawingBoundaryBottom;

    private @Nullable List<KeyLine> mKeyLineList;
    private boolean mKeyLineVisibility = true;

    private @Nullable Float mHighestData;
    private @Nullable Float mLowestData;

    private int mTextSize;
    private int mTextMargin;
    private int mLineWidth;

    private int mPointerId;
    private float mInitialX;
    private float mInitialY;
    private int mTouchSlop;
    private boolean mIsBeingDragged;
    private boolean mIsHorizontalDragged;

    private static final int LINE_WIDTH_DIP = 1;
    private static final int TEXT_SIZE_DIP = 10;
    private static final int TEXT_MARGIN_DIP = 2;
    public static final int ITEM_MARGIN_BOTTOM_DIP = 16;

    private static final String TAG = "TrendRecyclerView";

    public static class KeyLine {

        float value;
        String contentLeft;
        String contentRight;
        ContentPosition contentPosition;

        public enum ContentPosition {ABOVE_LINE, BELOW_LINE}

        public KeyLine(float value, String contentLeft, String contentRight, ContentPosition contentPosition) {
            this.value = value;
            this.contentLeft = contentLeft;
            this.contentRight = contentRight;
            this.contentPosition = contentPosition;
        }
    }

    public TrendRecyclerView(Context context) {
        super(context);
        initialize();
    }

    public TrendRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public TrendRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        setWillNotDraw(false);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mTextSize = (int) DisplayUtils.dpToPx(getContext(), TEXT_SIZE_DIP);
        mTextMargin = (int) DisplayUtils.dpToPx(getContext(), TEXT_MARGIN_DIP);
        mLineWidth = (int) DisplayUtils.dpToPx(getContext(), LINE_WIDTH_DIP);

        mDrawingBoundaryTop = -1;
        mDrawingBoundaryBottom = -1;

        setLineColor(Color.GRAY);

        mKeyLineList = new ArrayList<>();

        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawKeyLines(canvas);
    }

    private void drawKeyLines(Canvas canvas) {
        if (!mKeyLineVisibility
                || mKeyLineList == null
                || mKeyLineList.size() == 0
                || mHighestData == null
                || mLowestData == null) {
            return;
        }

        if (getChildCount() > 0) {
            mDrawingBoundaryTop = ((AbsTrendItemView) getChildAt(0)).getChartTop();
            mDrawingBoundaryBottom = ((AbsTrendItemView) getChildAt(0)).getChartBottom();
        }
        if (mDrawingBoundaryTop < 0 || mDrawingBoundaryBottom < 0) {
            return;
        }

        float dataRange = mHighestData - mLowestData;
        float boundaryRange = mDrawingBoundaryBottom - mDrawingBoundaryTop;
        for (KeyLine line : mKeyLineList) {
            if (line.value > mHighestData || line.value < mLowestData) {
                continue;
            }

            int y = (int) (mDrawingBoundaryBottom - (line.value - mLowestData) / dataRange * boundaryRange);

            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mLineWidth);
            mPaint.setColor(mLineColor);
            canvas.drawLine(0, y, getMeasuredWidth(), y, mPaint);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setTextSize(mTextSize);
            mPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorTextGrey2nd));
            switch (line.contentPosition) {
                case ABOVE_LINE:
                    mPaint.setTextAlign(Paint.Align.LEFT);
                    canvas.drawText(
                            line.contentLeft,
                            2 * mTextMargin,
                            y - mPaint.getFontMetrics().bottom - mTextMargin,
                            mPaint
                    );
                    mPaint.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText(
                            line.contentRight,
                            getMeasuredWidth() - 2 * mTextMargin,
                            y - mPaint.getFontMetrics().bottom - mTextMargin,
                            mPaint
                    );
                    break;

                case BELOW_LINE:
                    mPaint.setTextAlign(Paint.Align.LEFT);
                    canvas.drawText(
                            line.contentLeft,
                            2 * mTextMargin,
                            y - mPaint.getFontMetrics().top + mTextMargin,
                            mPaint
                    );
                    mPaint.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText(
                            line.contentRight,
                            getMeasuredWidth() - 2 * mTextMargin,
                            y - mPaint.getFontMetrics().top + mTextMargin,
                            mPaint
                    );
                    break;
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mIsBeingDragged = false;
                mIsHorizontalDragged = false;

                mPointerId = ev.getPointerId(0);
                mInitialX = ev.getX();
                mInitialY = ev.getY();

                getParent().requestDisallowInterceptTouchEvent(true);
                break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                int index = ev.getActionIndex();
                mPointerId = ev.getPointerId(index);
                mInitialX = ev.getX(index);
                mInitialY = ev.getY(index);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int index = ev.findPointerIndex(mPointerId);
                if (index == -1) {
                    Log.e(TAG, "Invalid pointerId=" + mPointerId + " in onTouchEvent");
                    break;
                }

                float x = ev.getX(index);
                float y = ev.getY(index);

                if (!mIsBeingDragged && !mIsHorizontalDragged) {
                    if (Math.abs(x - mInitialX) > mTouchSlop || Math.abs(y - mInitialY) > mTouchSlop) {
                        mIsBeingDragged = true;
                        if (Math.abs(x - mInitialX) > Math.abs(y - mInitialY)) {
                            mIsHorizontalDragged = true;
                        } else {
                            getParent().requestDisallowInterceptTouchEvent(false);
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int index = ev.getActionIndex();
                int id = ev.getPointerId(index);
                if (mPointerId == id) {
                    int newIndex = index == 0 ? 1 : 0;

                    this.mPointerId = ev.getPointerId(newIndex);
                    mInitialX = (int) ev.getX(newIndex);
                    mInitialY = (int) ev.getY(newIndex);
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mIsHorizontalDragged = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        return super.onInterceptTouchEvent(ev) && mIsBeingDragged && mIsHorizontalDragged;
    }

    // control.

    public void setData(List<KeyLine> keyLineList, float highestData, float lowestData) {
        mKeyLineList = keyLineList;
        mHighestData = highestData;
        mLowestData = lowestData;
        invalidate();
    }

    public void setKeyLineVisibility(boolean visibility) {
        mKeyLineVisibility = visibility;
        invalidate();
    }

    public void setLineColor(@ColorInt int lineColor) {
        mLineColor = lineColor;
        invalidate();
    }
}
