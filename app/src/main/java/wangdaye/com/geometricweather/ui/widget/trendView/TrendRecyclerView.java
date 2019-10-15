package wangdaye.com.geometricweather.ui.widget.trendView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.widget.trendView.i.TrendParent;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Trend recycler view.
 * */

public class TrendRecyclerView extends RecyclerView
        implements TrendParent {

    private Paint paint;
    @ColorInt private int lineColor;

    private int drawingBoundaryTop;
    private int drawingBoundaryBottom;

    private @Nullable Float maxiData;
    private @Nullable Float miniData;
    private @Nullable Float highestData;
    private @Nullable Float lowestData;
    private @Nullable String maxiDataStr;
    private @Nullable String miniDataStr;

    private int textSize;
    private int textMargin;
    private int lineWidth;

    private int pointerId;
    private float initialX;
    private float initialY;
    private int touchSlop;
    private boolean isBeingDragged;
    private boolean isHorizontalDragged;

    private static final int LINE_WIDTH_DIP = 1;
    private static final int TEXT_SIZE_DIP = 10;
    private static final int TEXT_MARGIN_DIP = 2;

    private static final String TAG = "TrendRecyclerView";

    public TrendRecyclerView(Context context) {
        super(context);
        this.initialize();
    }

    public TrendRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public TrendRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.initialize();
    }

    private void initialize() {
        setWillNotDraw(false);

        this.paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);

        this.textSize = (int) DisplayUtils.dpToPx(getContext(), TEXT_SIZE_DIP);
        this.textMargin = (int) DisplayUtils.dpToPx(getContext(), TEXT_MARGIN_DIP);
        this.lineWidth = (int) DisplayUtils.dpToPx(getContext(), LINE_WIDTH_DIP);

        setLineColor(Color.GRAY);

        this.touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isBeingDragged = false;
                isHorizontalDragged = false;

                pointerId = ev.getPointerId(0);
                initialX = ev.getX();
                initialY = ev.getY();
                break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                int index = ev.getActionIndex();
                pointerId = ev.getPointerId(index);
                initialX = ev.getX(index);
                initialY = ev.getY(index);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int index = ev.findPointerIndex(pointerId);
                if (index == -1) {
                    Log.e(TAG, "Invalid pointerId=" + pointerId + " in onTouchEvent");
                    break;
                }

                float x = ev.getX(index);
                float y = ev.getY(index);

                if (!isBeingDragged && !isHorizontalDragged) {
                    if (Math.abs(x - initialX) > touchSlop || Math.abs(y - initialY) > touchSlop) {
                        isBeingDragged = true;
                        if (Math.abs(x - initialX) > Math.abs(y - initialY)) {
                            isHorizontalDragged = true;
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int index = ev.getActionIndex();
                int id = ev.getPointerId(index);
                if (pointerId == id) {
                    int newIndex = index == 0 ? 1 : 0;

                    this.pointerId = ev.getPointerId(newIndex);
                    initialX = (int) ev.getX(newIndex);
                    initialY = (int) ev.getY(newIndex);
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isBeingDragged = false;
                isHorizontalDragged = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        return super.onInterceptTouchEvent(ev) && isBeingDragged && isHorizontalDragged;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (maxiData == null || TextUtils.isEmpty(maxiDataStr)
                || miniData == null || TextUtils.isEmpty(miniDataStr)
                || highestData == null
                || lowestData == null) {
            return;
        }

        float dataRange = highestData - lowestData;
        float boundaryRange = drawingBoundaryBottom - drawingBoundaryTop;
        int maxiY = (int) (drawingBoundaryBottom - (maxiData - lowestData) / dataRange * boundaryRange);
        int miniY = (int) (drawingBoundaryBottom - (miniData - lowestData) / dataRange * boundaryRange);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth);
        paint.setColor(lineColor);
        canvas.drawLine(0, maxiY, getMeasuredWidth(), maxiY, paint);
        canvas.drawLine(0, miniY, getMeasuredWidth(), miniY, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.colorTextGrey2nd));
        canvas.drawText(
                maxiDataStr,
                2 * textMargin,
                maxiY - paint.getFontMetrics().bottom - textMargin,
                paint
        );
        canvas.drawText(
                miniDataStr,
                2 * textMargin,
                miniY - paint.getFontMetrics().top + textMargin,
                paint
        );

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(
                getContext().getString(R.string.yesterday),
                getMeasuredWidth() - 2 * textMargin,
                maxiY - paint.getFontMetrics().bottom - textMargin,
                paint
        );
        canvas.drawText(
                getContext().getString(R.string.yesterday),
                getMeasuredWidth() - 2 * textMargin,
                miniY - paint.getFontMetrics().top + textMargin,
                paint
        );
    }

    // control.

    public void setData(@Nullable Float maxiData, @Nullable Float miniData,
                        float highestData, float lowestData,
                        @Nullable String maxiDataStr, @Nullable String miniDataStr) {
        this.maxiData = maxiData;
        this.miniData = miniData;
        this.highestData = highestData;
        this.lowestData = lowestData;
        this.maxiDataStr = maxiDataStr;
        this.miniDataStr = miniDataStr;
        invalidate();
    }

    public void setLineColor(@ColorInt int lineColor) {
        this.lineColor = lineColor;
        invalidate();
    }

    // interface.

    @Override
    public void setDrawingBoundary(int top, int bottom) {
        if (drawingBoundaryTop != top || drawingBoundaryBottom != bottom) {
            drawingBoundaryTop = top;
            drawingBoundaryBottom = bottom;
            invalidate();
        }
    }
}
