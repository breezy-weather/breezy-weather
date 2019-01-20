package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.ColorUtils;

import java.util.Random;

import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;

/**
 * Meteor shower implementor.
 * */

public class MeteorShowerImplementor extends MaterialWeatherView.WeatherAnimationImplementor {

    private Paint paint;
    private Meteor[] meteors;
    private Star[] stars;

    private float lastDisplayRate;

    private float lastRotation3D;
    private static final float INITIAL_ROTATION_3D = 1000;

    @ColorInt
    private int backgroundColor;

    private class Meteor {

        float x;
        float y;
        float width;
        float height;

        RectF rectF;
        float speed;

        @ColorInt
        int color;
        float scale;

        private int viewWidth;
        private int viewHeight;

        private int canvasSize;

        private final float MAX_HEIGHT;
        private final float MIN_HEIGHT;

        private Meteor(int viewWidth, int viewHeight, @ColorInt int color, float scale) { // 1, 0.7, 0.4
            this.viewWidth = viewWidth;
            this.viewHeight = viewHeight;

            this.canvasSize = (int) Math.pow(viewWidth * viewWidth + viewHeight * viewHeight, 0.5);

            this.width = (float) (viewWidth * 0.0088 * scale);

            this.rectF = new RectF();
            this.speed = (float) (viewHeight / 500.0);
            this.color = color;
            this.scale = scale;

            this.MAX_HEIGHT = (float) (1.1 * viewWidth / Math.cos(60.0 * Math.PI / 180.0));
            this.MIN_HEIGHT = (float) (MAX_HEIGHT * 0.7);

            this.init(true);
        }

        private void init(boolean firstTime) {
            Random r = new Random();
            x = r.nextInt(canvasSize);
            if (firstTime) {
                y = r.nextInt(canvasSize) - MAX_HEIGHT - canvasSize;
            } else {
                y = -MAX_HEIGHT;
            }
            height = MIN_HEIGHT + r.nextFloat() * (MAX_HEIGHT - MIN_HEIGHT);

            buildRectF();
        }

        private void buildRectF() {
            float x = (float) (this.x - (canvasSize - viewWidth) * 0.5);
            float y = (float) (this.y - (canvasSize - viewHeight) * 0.5);
            rectF.set(x, y, x + width, y + height);
        }

        void move(long interval, float deltaRotation3D) {
            x -= speed * interval * 5
                    * Math.sin(deltaRotation3D * Math.PI / 180.0) * Math.cos(60 * Math.PI / 180.0);
            y += speed * interval
                    * (Math.pow(scale, 0.5)
                    - 5 * Math.sin(deltaRotation3D * Math.PI / 180.0) * Math.sin(60 * Math.PI / 180.0));

            if (y >= canvasSize) {
                init(false);
            } else {
                buildRectF();
            }
        }
    }

    private class Star {

        float centerX;
        float centerY;
        float radius;

        @ColorInt
        int color;
        float alpha;

        long duration;
        long progress;

        Star(float centerX, float centerY, float radius,
             @ColorInt int color,
             long duration, long initProgress) {
            this.centerX = centerX;
            this.centerY = centerY;

            this.radius = (float) (radius * (0.7 + 0.3 * new Random().nextFloat()));

            this.color = color;

            this.duration = duration;
            this.progress = initProgress % duration;

            computeAlpha(duration, progress);
        }

        void shine(long interval) {
            progress = (progress + interval) % duration;
            computeAlpha(duration, progress);
        }

        private void computeAlpha(long duration, long progress) {
            if (progress < 0.5 * duration) {
                alpha = (float) (progress / 0.5 / duration);
            } else {
                alpha = (float) (1 - (progress - 0.5 * duration) / 0.5 / duration);
            }
        }
    }

    public MeteorShowerImplementor(MaterialWeatherView view) {
        this.paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        int viewWidth = view.getMeasuredWidth();
        int viewHeight = view.getMeasuredHeight();

        this.meteors = new Meteor[15];
        int[] colors = new int[]{
                Color.rgb(170, 215, 252),
                Color.rgb(255, 255, 255),
                Color.rgb(255, 255, 255)};
        float[] scales = new float[] {0.4F, 0.7F, 1};
        for (int i = 0; i < meteors.length; i ++) {
            meteors[i] = new Meteor(
                    viewWidth, viewHeight,
                    colors[i * 3 / meteors.length], scales[i * 3 / meteors.length]);
        }
        this.stars = new Star[30];
        Random r = new Random();
        int canvasSize = (int) Math.pow(
                Math.pow(viewWidth, 2) + Math.pow(viewHeight, 2),
                0.5);
        int width = (int) (1.0 * canvasSize);
        int height = (int) ((canvasSize - viewHeight) * 0.5 + viewWidth * 1.1111);
        float radius = (float) (0.0028 * viewWidth);
        int color = Color.rgb(255, 255, 255);
        for (int i = 0; i < stars.length; i ++) {
            int x = (int) (r.nextInt(width) - 0.5 * (canvasSize - viewWidth));
            int y = (int) (r.nextInt(height) - 0.5 * (canvasSize - viewHeight));
            boolean newPosition = true;
            for (int j = 0; j < i; j ++) {
                if (stars[j].centerX == x && stars[j].centerY == y) {
                    newPosition = false;
                    break;
                }
            }
            if (newPosition) {
                long duration = 1500 + r.nextInt(3) * 500;
                stars[i] = new Star(x, y, radius, color, duration, r.nextInt());
            } else {
                i --;
            }
        }

        this.lastDisplayRate = 0;
        this.lastRotation3D = INITIAL_ROTATION_3D;

        this.backgroundColor = Color.rgb(34, 45, 67);
    }

    @Override
    public void updateData(MaterialWeatherView view, long interval,
                           float rotation2D, float rotation3D) {

        for (Meteor m : meteors) {
            m.move(interval, lastRotation3D == INITIAL_ROTATION_3D ? 0 : rotation3D - lastRotation3D);
        }
        for (Star s : stars) {
            s.shine(interval);
        }
        lastRotation3D = rotation3D;
    }

    @Override
    public void draw(MaterialWeatherView view, Canvas canvas,
                     float displayRate, float scrollRate, float rotation2D, float rotation3D) {

        if (displayRate >= 1) {
            canvas.drawColor(backgroundColor);
        } else {
            canvas.drawColor(
                    ColorUtils.setAlphaComponent(
                            backgroundColor,
                            (int) (displayRate * 255)));
        }

        if (scrollRate < 1) {
            canvas.rotate(
                    rotation2D,
                    view.getMeasuredWidth() * 0.5F,
                    view.getMeasuredHeight() * 0.5F);
            for (Star s : stars) {
                paint.setColor(s.color);
                paint.setAlpha((int) (displayRate * (1 - scrollRate) * s.alpha * 255));
                canvas.drawCircle(s.centerX, s.centerY, s.radius, paint);
            }

            canvas.rotate(
                    60,
                    view.getMeasuredWidth() * 0.5F,
                    view.getMeasuredHeight() * 0.5F);
            for (Meteor m : meteors) {
                paint.setColor(m.color);
                if (displayRate < lastDisplayRate) {
                    paint.setAlpha((int) (displayRate * (1 - scrollRate) * 255));
                } else {
                    paint.setAlpha((int) ((1 - scrollRate) * 255));
                }
                canvas.drawRect(m.rectF, paint);
            }
        }

        lastDisplayRate = displayRate;
    }

    @ColorInt
    public static int getThemeColor() {
        return Color.rgb(34, 45, 67);
    }
}
