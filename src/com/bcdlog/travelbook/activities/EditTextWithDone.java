package com.bcdlog.travelbook.activities;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

public class EditTextWithDone extends EditText {

    public EditTextWithDone(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
    }

    public EditTextWithDone(Context context, AttributeSet attrs) {
	super(context, attrs);
    }

    public EditTextWithDone(Context context) {
	super(context);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
	InputConnection connection = super.onCreateInputConnection(outAttrs);
	int imeActions = outAttrs.imeOptions & EditorInfo.IME_MASK_ACTION;
	if ((imeActions & EditorInfo.IME_ACTION_DONE) != 0) {
	    // clear the existing action
	    outAttrs.imeOptions ^= imeActions;
	    // set the DONE action
	    outAttrs.imeOptions |= EditorInfo.IME_ACTION_DONE;
	}
	if ((outAttrs.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
	    outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
	}
	return connection;
    }
}
