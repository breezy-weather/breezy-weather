/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.common.snackbar

import android.os.Handler
import android.os.Looper
import android.os.Message

internal class SnackbarManager private constructor() {
    private val mLock: Any = Any()
    private val mHandler: Handler
    private var mCurrentRecord: SnackbarRecord? = null
    private var mNextRecord: SnackbarRecord? = null

    internal interface Callback {
        fun show()
        fun dismiss(event: Int)
    }

    init {
        mHandler = Handler(
            Looper.getMainLooper(),
            Handler.Callback { message: Message ->
                if (message.what == MSG_TIMEOUT) {
                    handleTimeout(message.obj as SnackbarRecord)
                    return@Callback true
                }
                false
            }
        )
    }

    fun show(duration: Int, callback: Callback) {
        synchronized(mLock) {
            if (isCurrentSnackbar(callback)) {
                // Means that the callback is already in the queue. We'll just update the duration
                mCurrentRecord?.mDuration = duration
                // If this is the Snackbar currently being shown, call re-schedule it's
                // timeout
                mHandler.removeCallbacksAndMessages(mCurrentRecord)
                scheduleTimeoutLocked(mCurrentRecord)
                return
            } else if (isNextSnackbar(callback)) {
                // We'll just update the duration
                mNextRecord?.mDuration = duration
            } else {
                // Else, we need to create a new record and queue it
                mNextRecord = SnackbarRecord(duration, callback)
            }
            if (mCurrentRecord != null &&
                cancelSnackbarLocked(mCurrentRecord, Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE)
            ) {
                // If we currently have a Snackbar, try and cancel it and wait in line
                return
            } else {
                // Clear out the current snackbar
                mCurrentRecord = null
                // Otherwise, just show it now
                showNextSnackbarLocked()
            }
        }
    }

    fun dismiss(callback: Callback, event: Int) {
        synchronized(mLock) {
            if (isCurrentSnackbar(callback)) {
                cancelSnackbarLocked(mCurrentRecord, event)
            } else if (isNextSnackbar(callback)) {
                cancelSnackbarLocked(mNextRecord, event)
            } else {}
        }
    }

    /**
     * Should be called when a Snackbar is no longer displayed. This is after any exit
     * animation has finished.
     */
    fun onDismissed(callback: Callback) {
        synchronized(mLock) {
            if (isCurrentSnackbar(callback)) {
                // If the callback is from a Snackbar currently show, remove it and show a new one
                mCurrentRecord = null
                if (mNextRecord != null) {
                    showNextSnackbarLocked()
                }
            }
        }
    }

    /**
     * Should be called when a Snackbar is being shown. This is after any entrance animation has
     * finished.
     */
    fun onShown(callback: Callback) {
        synchronized(mLock) {
            if (isCurrentSnackbar(callback)) {
                scheduleTimeoutLocked(mCurrentRecord)
            }
        }
    }

    fun cancelTimeout(callback: Callback) {
        synchronized(mLock) {
            if (isCurrentSnackbar(callback)) {
                mHandler.removeCallbacksAndMessages(mCurrentRecord)
            }
        }
    }

    fun restoreTimeout(callback: Callback) {
        synchronized(mLock) {
            if (isCurrentSnackbar(callback)) {
                scheduleTimeoutLocked(mCurrentRecord)
            }
        }
    }

    fun isCurrent(callback: Callback): Boolean {
        synchronized(mLock) { return isCurrentSnackbar(callback) }
    }

    fun isCurrentOrNext(callback: Callback): Boolean {
        synchronized(mLock) { return isCurrentSnackbar(callback) || isNextSnackbar(callback) }
    }

    private class SnackbarRecord(var mDuration: Int, val mCallback: Callback?) {
        fun isSnackbar(callback: Callback?): Boolean {
            return callback != null && mCallback === callback
        }
    }

    private fun showNextSnackbarLocked() {
        if (mNextRecord != null) {
            mCurrentRecord = mNextRecord
            mNextRecord = null
            mCurrentRecord?.mCallback?.show() ?: run {
                // The callback doesn't exist any more, clear out the Snackbar
                mCurrentRecord = null
            }
        }
    }

    private fun cancelSnackbarLocked(record: SnackbarRecord?, event: Int): Boolean {
        record?.mCallback?.let {
            it.dismiss(event)
            return true
        }
        return false
    }

    private fun isCurrentSnackbar(callback: Callback): Boolean {
        return mCurrentRecord?.isSnackbar(callback) ?: false
    }

    private fun isNextSnackbar(callback: Callback): Boolean {
        return mNextRecord?.isSnackbar(callback) ?: false
    }

    private fun scheduleTimeoutLocked(r: SnackbarRecord?) {
        if (r == null) return
        if (r.mDuration == Snackbar.LENGTH_INDEFINITE) {
            // If we're set to indefinite, we don't want to set a timeout
            return
        }
        var durationMs = LONG_DURATION_MS
        if (r.mDuration > 0) {
            durationMs = r.mDuration
        } else if (r.mDuration == Snackbar.LENGTH_SHORT) {
            durationMs = SHORT_DURATION_MS
        }
        mHandler.removeCallbacksAndMessages(r)
        mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_TIMEOUT, r), durationMs.toLong())
    }

    private fun handleTimeout(record: SnackbarRecord) {
        synchronized(mLock) {
            if (mCurrentRecord === record || mNextRecord === record) {
                cancelSnackbarLocked(record, Snackbar.Callback.DISMISS_EVENT_TIMEOUT)
            }
        }
    }

    companion object {
        val instance: SnackbarManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            SnackbarManager()
        }
        private const val MSG_TIMEOUT = 0
        private const val SHORT_DURATION_MS = 1500
        const val LONG_DURATION_MS = 3000
    }
}
