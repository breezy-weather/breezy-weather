package wangdaye.com.geometricweather.Widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.UI.MainActivity;

/**
 * Show the sky.
 * */

public class SkyView extends View {
    // widget
    private Context context;
    private Paint paint;

    // data
    private int drawTime;
    private boolean simpleShow;
    private boolean isTouch;
    private boolean isDay;

    private final double proportion = 6.8;

    private final int SHOW_FIRST_FLOOR_TIME = 15;
    private final int SHOW_OTHERS_FLOOR_TIME = 22;
    private final int SHOW_BACKGROUND_TIME = SHOW_FIRST_FLOOR_TIME + 3 * SHOW_OTHERS_FLOOR_TIME;

    private final int HIDE_FIRST_FLOOR_TIME = 6;
    private final int HIDE_SECOND_FLOOR_TIME = 15;
    private final int HIDE_THIRD_FLOOR_TIME = 25;
    private final int HIDE_FORTH_FLOOR_TIME = 34;
    private final int HIDE_BACKGROUND_TIME = 34;

    private final int TOUCH_DAY_UNIT_TIME = 6;
    private final int TOUCH_NIGHT_UNIT_TIME = 12;

    private boolean done;

    private int breathCycle;

    public SkyView(Context context) {
        super(context);
        this.initialize(context);
    }

    public SkyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize(context);
    }

    public SkyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SkyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize(context);
    }

    private void initialize(Context context) {
        this.context = context;
        this.paint = new Paint();

        this.isDay = MainActivity.isDay;
        this.readyToShow(true);

        this.setBreathData(0);
    }

    public void readyToShow(boolean simpleShow) {
        this.simpleShow = simpleShow;
        this.isTouch = false;
        this.drawTime = 0;
    }

    public void showCircle(boolean simpleShow) {
        readyToShow(simpleShow);
        invalidate();
    }

    public void touchCircle() {
        this.isTouch = true;
        invalidate();
    }

    public void setBreathData(int temp) {
        breathCycle = 600 - 30 * (temp + 30) / 5;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isTouch) {
            if (isDay) {
                touchCirclesDay(canvas);
            } else {
                touchCirclesNight(canvas);
            }
        }
        else if (simpleShow) {
            if (drawTime <= SHOW_FIRST_FLOOR_TIME) {
                showFirstFloor(canvas);
            } else if (drawTime <= SHOW_FIRST_FLOOR_TIME + SHOW_OTHERS_FLOOR_TIME) {
                showSecondFloor(canvas);
            } else if (drawTime <= SHOW_FIRST_FLOOR_TIME + 2 * SHOW_OTHERS_FLOOR_TIME) {
                showThirdFloor(canvas);
            } else if (drawTime <= SHOW_FIRST_FLOOR_TIME + 3 * SHOW_OTHERS_FLOOR_TIME) {
                showForthFloor(canvas);
            }
        } else {
            hideCircles(canvas);
        }
    }

    private void showFirstFloor(Canvas canvas) {
        float centerX = (float) (getMeasuredWidth() / 2.0);
        float centerY = (float) (getMeasuredHeight() / 2.0);

        float unitCircleSize = (float) (getMeasuredWidth() / proportion);
        float radius = 1 * unitCircleSize;
        float deltaRadius = radius / SHOW_FIRST_FLOOR_TIME;
        float deltaBackground = (float) (255.0 / SHOW_BACKGROUND_TIME);
        float radiusNow = deltaRadius * drawTime;
        float backgroundAlpha = deltaBackground * drawTime;

        if (isDay) {
            canvas.drawColor(Color.argb((int) backgroundAlpha, 117, 190, 203));
        } else {
            canvas.drawColor(Color.argb((int) backgroundAlpha, 26, 27, 34));
        }

        paint.reset();
        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_1));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_1));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawCircle(centerX, centerY, radiusNow, paint);
        paint.reset();

        drawTime ++;
        invalidate();
    }

    private void showSecondFloor(Canvas canvas) {
        float centerX = (float) (getMeasuredWidth() / 2.0);
        float centerY = (float) (getMeasuredHeight() / 2.0);

        float unitCircleSize = (float) (getMeasuredWidth() / proportion);
        float radius = 2 * unitCircleSize;
        float deltaRadius = radius / (SHOW_FIRST_FLOOR_TIME + SHOW_OTHERS_FLOOR_TIME);
        float deltaBackground = (float) (255.0 / SHOW_BACKGROUND_TIME);
        float radiusNow = deltaRadius * drawTime;
        float backgroundAlpha = deltaBackground * drawTime;

        if (isDay) {
            canvas.drawColor(Color.argb((int) backgroundAlpha, 117, 190, 203));
        } else {
            canvas.drawColor(Color.argb((int) backgroundAlpha, 26, 27, 34));
        }

        paint.reset();
        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_2));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_2));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawCircle(centerX, centerY, radiusNow, paint);
        paint.reset();

        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_1));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_1));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawCircle(centerX, centerY, radius / 2, paint);
        paint.reset();

        drawTime ++;
        invalidate();
    }

    private void showThirdFloor(Canvas canvas) {
        float centerX = (float) (getMeasuredWidth() / 2.0);
        float centerY = (float) (getMeasuredHeight() / 2.0);

        float unitCircleSize = (float) (getMeasuredWidth() / proportion);
        float radius = 3 * unitCircleSize;
        float deltaRadius = radius / (SHOW_FIRST_FLOOR_TIME + 2 * SHOW_OTHERS_FLOOR_TIME);
        float deltaBackground = (float) (255.0 / SHOW_BACKGROUND_TIME);
        float radiusNow = deltaRadius * drawTime;
        float backgroundAlpha = deltaBackground * drawTime;

        if (isDay) {
            canvas.drawColor(Color.argb((int) backgroundAlpha, 117, 190, 203));
        } else {
            canvas.drawColor(Color.argb((int) backgroundAlpha, 26, 27, 34));
        }

        paint.reset();
        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_3));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_3));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawCircle(centerX, centerY, radiusNow, paint);
        paint.reset();

        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_2));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_2));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawCircle(centerX, centerY, radius / 3 * 2, paint);
        paint.reset();

        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_1));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_1));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawCircle(centerX, centerY, radius / 3, paint);
        paint.reset();

        drawTime ++;
        invalidate();
    }

    private void showForthFloor(Canvas canvas) {
        float centerX = (float) (getMeasuredWidth() / 2.0);
        float centerY = (float) (getMeasuredHeight() / 2.0);

        float unitCircleSize = (float) (getMeasuredWidth() / proportion);
        float radius = 4 * unitCircleSize;
        float deltaRadius = radius / (SHOW_FIRST_FLOOR_TIME + 3 * SHOW_OTHERS_FLOOR_TIME);
        float deltaBackground = (float) (255.0 / SHOW_BACKGROUND_TIME);
        float radiusNow = deltaRadius * drawTime;
        float backgroundAlpha = deltaBackground * drawTime;

        if (isDay) {
            canvas.drawColor(Color.argb((int) backgroundAlpha, 117, 190, 203));
        } else {
            canvas.drawColor(Color.argb((int) backgroundAlpha, 26, 27, 34));
        }

        paint.reset();
        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_4));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_4));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawCircle(centerX, centerY, radiusNow, paint);
        paint.reset();

        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_3));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_3));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawCircle(centerX, centerY, radius / 4 * 3, paint);
        paint.reset();

        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_2));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_2));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawCircle(centerX, centerY, radius / 2, paint);
        paint.reset();

        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_1));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_1));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawCircle(centerX, centerY, radius / 4, paint);
        paint.reset();

        if (drawTime != SHOW_BACKGROUND_TIME - 1) {
            drawTime ++;
            invalidate();
        } else {
            this.readyToShow(true);
        }
    }

    private void hideCircles(Canvas canvas) {
        float centerX = (float) (getMeasuredWidth() / 2.0);
        float centerY = (float) (getMeasuredHeight() / 2.0);

        float unitCircleSize = (float) (getMeasuredWidth() / proportion);
        float deltaBackground = (float) (255.0 / HIDE_BACKGROUND_TIME);
        float backgroundAlpha = 255 - deltaBackground * drawTime;

        float radius;
        float deltaRadius;
        float radiusNow;

        if (isDay) {
            canvas.drawColor(Color.argb((int) backgroundAlpha, 117, 190, 203));
        } else {
            canvas.drawColor(Color.argb((int) backgroundAlpha, 26, 27, 34));
        }

        paint.reset();
        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_4));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_4));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        radius = 4 * unitCircleSize;
        deltaRadius = radius / HIDE_FORTH_FLOOR_TIME;
        radiusNow = radius - deltaRadius * drawTime;
        if (radiusNow < 0) {
            radiusNow = 0;
        }
        canvas.drawCircle(centerX, centerY, radiusNow, paint);
        paint.reset();

        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_3));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_3));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        radius = 3 * unitCircleSize;
        deltaRadius = radius / HIDE_THIRD_FLOOR_TIME;
        radiusNow = radius - deltaRadius * drawTime;
        if (radiusNow < 0) {
            radiusNow = 0;
        }
        canvas.drawCircle(centerX, centerY, radiusNow, paint);
        paint.reset();

        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_2));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_2));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        radius = 2 * unitCircleSize;
        deltaRadius = radius / HIDE_SECOND_FLOOR_TIME;
        radiusNow = radius - deltaRadius * drawTime;
        if (radiusNow < 0) {
            radiusNow = 0;
        }
        canvas.drawCircle(centerX, centerY, radiusNow, paint);
        paint.reset();

        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_1));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_1));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        radius = 1 * unitCircleSize;
        deltaRadius = radius / HIDE_FIRST_FLOOR_TIME;
        radiusNow = radius - deltaRadius * drawTime;
        if (radiusNow < 0) {
            radiusNow = 0;
        }
        canvas.drawCircle(centerX, centerY, radiusNow, paint);
        paint.reset();

        if (drawTime == HIDE_BACKGROUND_TIME - 1) {
            readyToShow(true);
            isDay = MainActivity.isDay;
        } else {
            drawTime ++;
        }
        invalidate();
    }

    private void touchCirclesDay(Canvas canvas) {
        float centerX = (float) (getMeasuredWidth() / 2.0);
        float centerY = (float) (getMeasuredHeight() / 2.0);

        float unitCircleSize = (float) (getMeasuredWidth() / proportion);

        float radius;
        float deltaRadius;
        float radiusNow;

        paint.reset();
        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_5));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_5));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), paint);
        paint.reset();

        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_4));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_4));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        radius = 4 * unitCircleSize;
        if (3 * TOUCH_DAY_UNIT_TIME <= drawTime && drawTime < 4 * TOUCH_DAY_UNIT_TIME) {
            deltaRadius = (float) (0.6 * unitCircleSize / TOUCH_DAY_UNIT_TIME);
            radiusNow = radius + deltaRadius * (drawTime - 3 * TOUCH_DAY_UNIT_TIME);
        } else if (4 * TOUCH_DAY_UNIT_TIME <= drawTime && drawTime < 5 *TOUCH_DAY_UNIT_TIME) {
            deltaRadius = (float) (0.8 * unitCircleSize / TOUCH_DAY_UNIT_TIME);
            radiusNow = (float) (4.6 * unitCircleSize - deltaRadius * (drawTime - 4 * TOUCH_DAY_UNIT_TIME));
        } else if (5 * TOUCH_DAY_UNIT_TIME <= drawTime && drawTime < 6 *TOUCH_DAY_UNIT_TIME) {
            deltaRadius = (float) (0.2 * unitCircleSize / TOUCH_DAY_UNIT_TIME);
            radiusNow = (float) (3.8 * unitCircleSize + deltaRadius * (drawTime - 5 * TOUCH_DAY_UNIT_TIME));
        } else {
            radiusNow = radius;
        }
        canvas.drawCircle(centerX, centerY, radiusNow, paint);
        paint.reset();

        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_3));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_3));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        radius = 3 * unitCircleSize;
        if (2 * TOUCH_DAY_UNIT_TIME <= drawTime && drawTime < 3 * TOUCH_DAY_UNIT_TIME) {
            deltaRadius = (float) (0.5 * unitCircleSize / TOUCH_DAY_UNIT_TIME);
            radiusNow = radius + deltaRadius * (drawTime - 2 * TOUCH_DAY_UNIT_TIME);
        } else if (3 * TOUCH_DAY_UNIT_TIME <= drawTime && drawTime < 4 *TOUCH_DAY_UNIT_TIME) {
            deltaRadius = (float) (0.7 * unitCircleSize / TOUCH_DAY_UNIT_TIME);
            radiusNow = (float) (3.5 * unitCircleSize - deltaRadius * (drawTime - 3 * TOUCH_DAY_UNIT_TIME));
        } else if (4 * TOUCH_DAY_UNIT_TIME <= drawTime && drawTime < 5 *TOUCH_DAY_UNIT_TIME) {
            deltaRadius = (float) (0.2 * unitCircleSize / TOUCH_DAY_UNIT_TIME);
            radiusNow = (float) (2.8 * unitCircleSize + deltaRadius * (drawTime - 4 * TOUCH_DAY_UNIT_TIME));
        } else {
            radiusNow = radius;
        }
        canvas.drawCircle(centerX, centerY, radiusNow, paint);
        paint.reset();

        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_2));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_2));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        radius = 2 * unitCircleSize;
        if (TOUCH_DAY_UNIT_TIME <= drawTime && drawTime < 2 * TOUCH_DAY_UNIT_TIME) {
            deltaRadius = (float) (0.4 * unitCircleSize / TOUCH_DAY_UNIT_TIME);
            radiusNow = radius + deltaRadius * (drawTime - TOUCH_DAY_UNIT_TIME);
        } else if (2 * TOUCH_DAY_UNIT_TIME <= drawTime && drawTime < 3 *TOUCH_DAY_UNIT_TIME) {
            deltaRadius = (float) (0.6 * unitCircleSize / TOUCH_DAY_UNIT_TIME);
            radiusNow = (float) (2.4 * unitCircleSize - deltaRadius * (drawTime - 2 * TOUCH_DAY_UNIT_TIME));
        } else if (3 * TOUCH_DAY_UNIT_TIME <= drawTime && drawTime < 4 *TOUCH_DAY_UNIT_TIME) {
            deltaRadius = (float) (0.2 * unitCircleSize / TOUCH_DAY_UNIT_TIME);
            radiusNow = (float) (1.8 * unitCircleSize + deltaRadius * (drawTime - 3 * TOUCH_DAY_UNIT_TIME));
        } else {
            radiusNow = radius;
        }
        canvas.drawCircle(centerX, centerY, radiusNow, paint);
        paint.reset();

        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_1));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_1));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        radius = 1 * unitCircleSize;
        if (drawTime < TOUCH_DAY_UNIT_TIME) {
            deltaRadius = (float) (0.3 * unitCircleSize / TOUCH_DAY_UNIT_TIME);
            radiusNow = radius + deltaRadius * drawTime;
        } else if (TOUCH_DAY_UNIT_TIME <= drawTime && drawTime < 2 *TOUCH_DAY_UNIT_TIME) {
            deltaRadius = (float) (0.5 * unitCircleSize / TOUCH_DAY_UNIT_TIME);
            radiusNow = (float) (1.3 * unitCircleSize - deltaRadius * (drawTime - TOUCH_DAY_UNIT_TIME));
        } else if (2 * TOUCH_DAY_UNIT_TIME <= drawTime && drawTime < 3 *TOUCH_DAY_UNIT_TIME) {
            deltaRadius = (float) (0.2 * unitCircleSize / TOUCH_DAY_UNIT_TIME);
            radiusNow = (float) (0.8 * unitCircleSize + deltaRadius * (drawTime - 2 * TOUCH_DAY_UNIT_TIME));
        } else {
            radiusNow = radius;
        }
        canvas.drawCircle(centerX, centerY, radiusNow, paint);
        paint.reset();

        if (drawTime == 6 * TOUCH_DAY_UNIT_TIME - 1) {
            readyToShow(true);
        } else {
            drawTime ++;
            invalidate();
        }
    }

    private void touchCirclesNight(Canvas canvas) {
        float centerX = (float) (getMeasuredWidth() / 2.0);
        float centerY = (float) (getMeasuredHeight() / 2.0);

        float unitCircleSize = (float) (getMeasuredWidth() / proportion);

        float radius;
        float deltaRadius;
        float radiusNow;

        paint.reset();
        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_5));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_5));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), paint);
        paint.reset();

        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_4));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_4));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        radius = 4 * unitCircleSize;
        if (3 * TOUCH_NIGHT_UNIT_TIME <= drawTime && drawTime < 4 * TOUCH_NIGHT_UNIT_TIME) {
            deltaRadius = (float) (0.1 * unitCircleSize / TOUCH_NIGHT_UNIT_TIME);
            radiusNow = radius + deltaRadius * (drawTime - 3 * TOUCH_NIGHT_UNIT_TIME);
        } else if (4 * TOUCH_NIGHT_UNIT_TIME <= drawTime && drawTime < 5 *TOUCH_NIGHT_UNIT_TIME) {
            deltaRadius = (float) (0.2 * unitCircleSize / TOUCH_NIGHT_UNIT_TIME);
            radiusNow = (float) (4.1 * unitCircleSize - deltaRadius * (drawTime - 4 * TOUCH_NIGHT_UNIT_TIME));
        } else if (5 *TOUCH_NIGHT_UNIT_TIME <= drawTime && drawTime < 6 * TOUCH_NIGHT_UNIT_TIME) {
            deltaRadius = (float) (0.1 * unitCircleSize / TOUCH_NIGHT_UNIT_TIME);
            radiusNow = (float) (3.9 * unitCircleSize + deltaRadius * (drawTime - 5 * TOUCH_NIGHT_UNIT_TIME));
        } else {
            radiusNow = radius;
        }
        canvas.drawCircle(centerX, centerY, radiusNow, paint);
        paint.reset();

        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_3));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_3));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        radius = 3 * unitCircleSize;
        if (2 * TOUCH_NIGHT_UNIT_TIME <= drawTime && drawTime < 3 * TOUCH_NIGHT_UNIT_TIME) {
            deltaRadius = (float) (0.2 * unitCircleSize / TOUCH_NIGHT_UNIT_TIME);
            radiusNow = radius + deltaRadius * (drawTime - 2 * TOUCH_NIGHT_UNIT_TIME);
        } else if (3 * TOUCH_NIGHT_UNIT_TIME <= drawTime && drawTime < 4 *TOUCH_NIGHT_UNIT_TIME) {
            deltaRadius = (float) (0.3 * unitCircleSize / TOUCH_NIGHT_UNIT_TIME);
            radiusNow = (float) (3.2 * unitCircleSize - deltaRadius * (drawTime - 3 * TOUCH_NIGHT_UNIT_TIME));
        } else if (4 * TOUCH_NIGHT_UNIT_TIME <= drawTime && drawTime < 5 *TOUCH_NIGHT_UNIT_TIME) {
            deltaRadius = (float) (0.1 * unitCircleSize / TOUCH_NIGHT_UNIT_TIME);
            radiusNow = (float) (2.9 * unitCircleSize + deltaRadius * (drawTime - 4 * TOUCH_NIGHT_UNIT_TIME));
        } else {
            radiusNow = radius;
        }
        canvas.drawCircle(centerX, centerY, radiusNow, paint);
        paint.reset();

        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_2));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_2));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        radius = 2 * unitCircleSize;
        if (TOUCH_NIGHT_UNIT_TIME <= drawTime && drawTime < 2 * TOUCH_NIGHT_UNIT_TIME) {
            deltaRadius = (float) (0.3 * unitCircleSize / TOUCH_NIGHT_UNIT_TIME);
            radiusNow = radius + deltaRadius * (drawTime - TOUCH_NIGHT_UNIT_TIME);
        } else if (2 * TOUCH_NIGHT_UNIT_TIME <= drawTime && drawTime < 3 *TOUCH_NIGHT_UNIT_TIME) {
            deltaRadius = (float) (0.4 * unitCircleSize / TOUCH_NIGHT_UNIT_TIME);
            radiusNow = (float) (2.3 * unitCircleSize - deltaRadius * (drawTime - 2 * TOUCH_NIGHT_UNIT_TIME));
        } else if (3 * TOUCH_NIGHT_UNIT_TIME <= drawTime && drawTime < 4 *TOUCH_NIGHT_UNIT_TIME) {
            deltaRadius = (float) (0.1 * unitCircleSize / TOUCH_NIGHT_UNIT_TIME);
            radiusNow = (float) (1.9 * unitCircleSize + deltaRadius * (drawTime - 3 * TOUCH_NIGHT_UNIT_TIME));
        } else {
            radiusNow = radius;
        }
        canvas.drawCircle(centerX, centerY, radiusNow, paint);
        paint.reset();

        if (isDay) {
            paint.setColor(ContextCompat.getColor(context, R.color.lightPrimary_1));
        } else {
            paint.setColor(ContextCompat.getColor(context, R.color.darkPrimary_1));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        radius = 1 * unitCircleSize;
        if (drawTime < TOUCH_NIGHT_UNIT_TIME) {
            deltaRadius = (float) (0.4 * unitCircleSize / TOUCH_NIGHT_UNIT_TIME);
            radiusNow = radius + deltaRadius * drawTime;
        } else if (TOUCH_NIGHT_UNIT_TIME <= drawTime && drawTime < 2 *TOUCH_NIGHT_UNIT_TIME) {
            deltaRadius = (float) (0.5 * unitCircleSize / TOUCH_NIGHT_UNIT_TIME);
            radiusNow = (float) (1.4 * unitCircleSize - deltaRadius * (drawTime - TOUCH_NIGHT_UNIT_TIME));
        } else if (2 * TOUCH_NIGHT_UNIT_TIME <= drawTime && drawTime < 3 *TOUCH_NIGHT_UNIT_TIME) {
            deltaRadius = (float) (0.1 * unitCircleSize / TOUCH_NIGHT_UNIT_TIME);
            radiusNow = (float) (0.9 * unitCircleSize + deltaRadius * (drawTime - 2 * TOUCH_NIGHT_UNIT_TIME));
        } else {
            radiusNow = radius;
        }
        canvas.drawCircle(centerX, centerY, radiusNow, paint);
        paint.reset();

        if (drawTime == 6 * TOUCH_NIGHT_UNIT_TIME - 1) {
            readyToShow(true);
        } else {
            drawTime ++;
            invalidate();
        }
    }
}