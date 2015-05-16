package com.bcdlog.travelbook.activities.gallery;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;

public class TBGallery extends Gallery {

    public TBGallery(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
    }

    public TBGallery(Context context, AttributeSet attrs) {
	super(context, attrs);
    }

    public TBGallery(Context context) {
	super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
	this.onTouchEvent(ev);
	return false;
    }

}
