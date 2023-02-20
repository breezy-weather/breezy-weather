package wangdaye.com.geometricweather.common.snackbar;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

class SnackbarManager {

    private static SnackbarManager sInstance;

    static SnackbarManager getInstance() {
        if (sInstance == null) {
            sInstance = new SnackbarManager();
        }
        return sInstance;
    }

    private static final int MSG_TIMEOUT = 0;

    private static final int SHORT_DURATION_MS = 1500;
    private static final int LONG_DURATION_MS = 3000;

    private final Object mLock;
    private final Handler mHandler;

    private SnackbarRecord mCurrentRecord;
    private SnackbarRecord mNextRecord;

    interface Callback {
        void show();
        void dismiss(int event);
    }

    private SnackbarManager() {
        mLock = new Object();
        mHandler = new Handler(Looper.getMainLooper(), message -> {
            if (message.what == MSG_TIMEOUT) {
                handleTimeout((SnackbarRecord) message.obj);
                return true;
            }
            return false;
        });
    }

    public void show(int duration, Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbar(callback)) {
                // Means that the callback is already in the queue. We'll just update the duration
                mCurrentRecord.mDuration = duration;
                // If this is the Snackbar currently being shown, call re-schedule it's
                // timeout
                mHandler.removeCallbacksAndMessages(mCurrentRecord);
                scheduleTimeoutLocked(mCurrentRecord);
                return;
            } else if (isNextSnackbar(callback)) {
                // We'll just update the duration
                mNextRecord.mDuration = duration;
            } else {
                // Else, we need to create a new record and queue it
                mNextRecord = new SnackbarRecord(duration, callback);
            }

            if (mCurrentRecord != null && cancelSnackbarLocked(mCurrentRecord,
                    Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE)) {
                // If we currently have a Snackbar, try and cancel it and wait in line
                return;
            } else {
                // Clear out the current snackbar
                mCurrentRecord = null;
                // Otherwise, just show it now
                showNextSnackbarLocked();
            }
        }
    }

    public void dismiss(Callback callback, int event) {
        synchronized (mLock) {
            if (isCurrentSnackbar(callback)) {
                cancelSnackbarLocked(mCurrentRecord, event);
            } else if (isNextSnackbar(callback)) {
                cancelSnackbarLocked(mNextRecord, event);
            }
        }
    }

    /**
     * Should be called when a Snackbar is no longer displayed. This is after any exit
     * animation has finished.
     */
    public void onDismissed(Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbar(callback)) {
                // If the callback is from a Snackbar currently show, remove it and show a new one
                mCurrentRecord = null;
                if (mNextRecord != null) {
                    showNextSnackbarLocked();
                }
            }
        }
    }

    /**
     * Should be called when a Snackbar is being shown. This is after any entrance animation has
     * finished.
     */
    public void onShown(Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbar(callback)) {
                scheduleTimeoutLocked(mCurrentRecord);
            }
        }
    }

    public void cancelTimeout(Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbar(callback)) {
                mHandler.removeCallbacksAndMessages(mCurrentRecord);
            }
        }
    }

    public void restoreTimeout(Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbar(callback)) {
                scheduleTimeoutLocked(mCurrentRecord);
            }
        }
    }

    public boolean isCurrent(Callback callback) {
        synchronized (mLock) {
            return isCurrentSnackbar(callback);
        }
    }

    public boolean isCurrentOrNext(Callback callback) {
        synchronized (mLock) {
            return isCurrentSnackbar(callback) || isNextSnackbar(callback);
        }
    }

    private static class SnackbarRecord {
        private final Callback mCallback;
        private int mDuration;

        SnackbarRecord(int duration, Callback callback) {
            mCallback = callback;
            mDuration = duration;
        }

        boolean isSnackbar(Callback callback) {
            return callback != null && mCallback == callback;
        }
    }

    private void showNextSnackbarLocked() {
        if (mNextRecord != null) {
            mCurrentRecord = mNextRecord;
            mNextRecord = null;

            final Callback callback = mCurrentRecord.mCallback;
            if (callback != null) {
                callback.show();
            } else {
                // The callback doesn't exist any more, clear out the Snackbar
                mCurrentRecord = null;
            }
        }
    }

    private boolean cancelSnackbarLocked(SnackbarRecord record, int event) {
        final Callback callback = record.mCallback;
        if (callback != null) {
            callback.dismiss(event);
            return true;
        }
        return false;
    }

    private boolean isCurrentSnackbar(Callback callback) {
        return mCurrentRecord != null && mCurrentRecord.isSnackbar(callback);
    }

    private boolean isNextSnackbar(Callback callback) {
        return mNextRecord != null && mNextRecord.isSnackbar(callback);
    }

    private void scheduleTimeoutLocked(SnackbarRecord r) {
        if (r.mDuration == Snackbar.LENGTH_INDEFINITE) {
            // If we're set to indefinite, we don't want to set a timeout
            return;
        }

        int durationMs = LONG_DURATION_MS;
        if (r.mDuration > 0) {
            durationMs = r.mDuration;
        } else if (r.mDuration == Snackbar.LENGTH_SHORT) {
            durationMs = SHORT_DURATION_MS;
        }
        mHandler.removeCallbacksAndMessages(r);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_TIMEOUT, r), durationMs);
    }

    private void handleTimeout(SnackbarRecord record) {
        synchronized (mLock) {
            if (mCurrentRecord == record || mNextRecord == record) {
                cancelSnackbarLocked(record, Snackbar.Callback.DISMISS_EVENT_TIMEOUT);
            }
        }
    }

}