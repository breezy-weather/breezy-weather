package org.breezyweather.main.widgets;

import static android.widget.RemoteViews.RemoteView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;

import android.widget.TextView;
import org.breezyweather.common.utils.DisplayUtils;

import java.util.Date;

/**
 * <p><code>TextRelativeClock</code> can display the current relative time as a formatted string.</p>
 */
@SuppressLint("AppCompatCustomView")
@RemoteView
public class TextRelativeClock extends TextView {
    private boolean mShouldRunTicker;

    private Date mDate = new Date();

    private final Runnable mTicker = new Runnable() {
        public void run() {
            removeCallbacks(this);
            if (!mShouldRunTicker) {
                return;
            }
            onTimeChanged();

            //Date now = new Date();

            long millisUntilNextTick = 60 * 1000;
            // It is currently refreshing every minute
            // It's not precise (but enough for our use case) as it won't refresh on second 0 of next minute
            // but rather on the same second on next minute
            // Plus it's refreshing every minute when > 1 hour, which is not optimized
            // TODO: We should optimize this function one day, for Green IT purposes
            /*long secondsDifference = (now.getTime() - mDate.getTime()) / 1000;
            if (secondsDifference <= 60 * 60) { // < 1 hour

            } else if (secondsDifference <= 24 * 60 * 60) { // < 24 hours
                // Calculate modulo for next hour
            } else { // More than 24 hours

            }*/

            postDelayed(this, millisUntilNextTick);
        }
    };

    @SuppressWarnings("UnusedDeclaration")
    public TextRelativeClock(Context context) {
        super(context);
        init();
    }

    @SuppressWarnings("UnusedDeclaration")
    public TextRelativeClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextRelativeClock(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TextRelativeClock(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        runTicker();
    }

    public void setDate(Date date) {
        mDate = date;
        onTimeChanged();
    }

    /**
     * Run ticker if required
     */
    private void runTicker() {
        if (mShouldRunTicker) {
            mTicker.run();
        }
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);

        if (!mShouldRunTicker && isVisible) {
            mShouldRunTicker = true;
            mTicker.run();
        } else if (mShouldRunTicker && !isVisible) {
            mShouldRunTicker = false;
            removeCallbacks(mTicker);
        }
    }

    /**
     * Update the displayed time if this view and its ancestors and window is visible
     */
    private void onTimeChanged() {
        String relativeTimeFormatted = DisplayUtils.getRelativeTime(mDate);
        setText(relativeTimeFormatted);
        setContentDescription(relativeTimeFormatted);
    }
}
