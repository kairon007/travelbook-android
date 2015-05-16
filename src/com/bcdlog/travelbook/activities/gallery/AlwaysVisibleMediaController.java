package com.bcdlog.travelbook.activities.gallery;

import android.content.Context;
import android.widget.MediaController;

public class AlwaysVisibleMediaController extends MediaController {

    public AlwaysVisibleMediaController(Context context) {
	super(context);
    }

    @Override
    public void hide() {
	// Never hide
    }

    public void hideNow() {
	super.hide();
    }

}
