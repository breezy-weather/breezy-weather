package org.breezyweather.common.ui.widgets.horizontal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class HorizontalRecyclerView extends RecyclerView {

    private int mPointerId;
    private float mInitialX;
    private float mInitialY;
    private final int mTouchSlop;
    private boolean mBeingDragged;
    private boolean mHorizontalDragged;

    private static final String TAG = "HorizontalRecyclerView";

    public HorizontalRecyclerView(Context context) {
        this(context, null);
    }

    public HorizontalRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mBeingDragged = false;
                mHorizontalDragged = false;

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

                if (!mBeingDragged && !mHorizontalDragged) {
                    if (Math.abs(x - mInitialX) > mTouchSlop || Math.abs(y - mInitialY) > mTouchSlop) {
                        mBeingDragged = true;
                        if (Math.abs(x - mInitialX) > Math.abs(y - mInitialY)) {
                            mHorizontalDragged = true;
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
                mBeingDragged = false;
                mHorizontalDragged = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        return super.onInterceptTouchEvent(ev) && mBeingDragged && mHorizontalDragged;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
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

                if (!mBeingDragged && !mHorizontalDragged) {
                    mBeingDragged = true;
                    if (Math.abs(x - mInitialX) > Math.abs(y - mInitialY)) {
                        mHorizontalDragged = true;
                    } else {
                        getParent().requestDisallowInterceptTouchEvent(false);
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
                mBeingDragged = false;
                mHorizontalDragged = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        return super.onTouchEvent(ev);
    }
}
