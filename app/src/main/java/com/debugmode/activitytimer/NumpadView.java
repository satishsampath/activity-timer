package com.debugmode.activitytimer;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Date;

/**
 * TODO: document your custom view class.
 */
public class NumpadView extends LinearLayout {

    public interface Listener {
        public void onNumpadString(String str);
    }

    private int[] mNumpadIds = {
            R.id.numpad0, R.id.numpad1, R.id.numpad2, R.id.numpad3, R.id.numpad4,
            R.id.numpad5, R.id.numpad6, R.id.numpad7, R.id.numpad8, R.id.numpad9
    };

    private String mNumpadString;
    private long mLastNumpadKeyMillis = 0;
    private int mClearAfterMillis = 0;
    private Listener mListener;
    private View.OnClickListener mNumpadClickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            long currentTimeMillis = new Date().getTime();
            if (mClearAfterMillis > 0 &&
                    currentTimeMillis - mLastNumpadKeyMillis > mClearAfterMillis) {
                mNumpadString = "";  // been too late since last key press, so start afresh
            }
            mLastNumpadKeyMillis = currentTimeMillis;
            for (int i = 0; i < mNumpadIds.length; ++i) {
                if (v.getId() == mNumpadIds[i]) {
                    mNumpadString = mNumpadString + String.valueOf(i);
                    break;
                }
            }
            if (mListener != null)
                mListener.onNumpadString(mNumpadString);
        }
    };

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public NumpadView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public NumpadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public NumpadView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        inflate(context, R.layout.numpad_view, this);

        // Load attributes
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.NumpadView, defStyle, 0);
        mClearAfterMillis = a.getInteger(R.styleable.NumpadView_clearAfterMillis, 0);
        int numSize = a.getDimensionPixelSize(R.styleable.NumpadView_size, 0);
        a.recycle();

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(numSize, numSize);
        for (int id : mNumpadIds) {
            ImageView iv = (ImageView) findViewById(id);
            iv.setLayoutParams(lp);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iv.setOnClickListener(mNumpadClickHandler);
        }
    }

}
