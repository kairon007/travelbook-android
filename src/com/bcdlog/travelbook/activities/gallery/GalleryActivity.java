package com.bcdlog.travelbook.activities.gallery;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;

import com.bcdlog.travelbook.R;
import com.bcdlog.travelbook.TBPreferences;
import com.bcdlog.travelbook.activities.items.ItemsListFragment;

public class GalleryActivity extends Activity {

    private EditText descriptionEdit;
    private Menu menu;
    private GalleryAdapter galleryAdapter;
    private TBGallery gallery;
    private Long userId;
    private Bundle savedInstanceState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	this.savedInstanceState = savedInstanceState;
	setContentView(R.layout.gallery);

	getActionBar().setHomeButtonEnabled(true);

	descriptionEdit = (EditText) findViewById(R.id.description_edit);

	Bundle bundle = this.getIntent().getExtras();
	userId = bundle.getLong("userId");

	gallery = (TBGallery) findViewById(R.id.gallery);
	galleryAdapter = new GalleryAdapter(this, userId);
	gallery.setAdapter(galleryAdapter);

	gallery.setOnItemSelectedListener(galleryAdapter);
	gallery.setOnItemClickListener(new OnItemClickListener() {
	    @Override
	    public void onItemClick(AdapterView parent, View v, int position,
		    long id) {
		swapOverlay();
	    }
	});

	descriptionEdit.setHeight(getWindowManager().getDefaultDisplay()
		.getHeight() / 2);
    }

    @Override
    protected void onResume() {
	super.onResume();
	Bundle bundle = this.getIntent().getExtras();
	if (savedInstanceState != null) {
	    // When back from playing audio or video
	    return;
	    // bundle = savedInstanceState;
	}

	// Restore position
	gallery.setSelection(bundle.getInt("position"));
	galleryAdapter.onItemSelected(null,
		galleryAdapter.getView(bundle.getInt("position"), null, null),
		bundle.getInt("position"), 0);

	// Restore fullscreen state
	Boolean fullscreen = bundle.getBoolean("fullscreen");
	if (fullscreen) {
	    swapOverlay();
	}

	// Restore editor
	String description = bundle.getString("description");
	if (description != null) {
	    showEditor(description);
	}

    }

    protected void swapOverlay() {
	ActionBar actionBar = getActionBar();
	if (actionBar.isShowing()) {
	    actionBar.hide();
	    hideEditor();
	    galleryAdapter.changeTextVisibility(false);
	} else {
	    actionBar.show();
	    galleryAdapter.changeTextVisibility(true);
	}
    }

    private void hideEditor() {
	descriptionEdit.setVisibility(View.INVISIBLE);
	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	imm.hideSoftInputFromWindow(descriptionEdit.getWindowToken(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	if (userId.equals(TBPreferences.getUserId())) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.cancel, menu);
	    inflater.inflate(R.menu.edit, menu);
	    inflater.inflate(R.menu.save, menu);
	    this.menu = menu;
	    menu.getItem(0).setVisible(false);
	    menu.getItem(2).setVisible(false);
	    return true;
	} else {
	    return false;
	}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
	switch (menuItem.getItemId()) {
	case android.R.id.home:
	    finish();
	    return true;
	case R.id.edit:
	    showEditor(galleryAdapter.getCurrentItem().getContent());
	    return true;
	case R.id.save:
	    closeEditor();
	    galleryAdapter
		    .saveDescription(descriptionEdit.getText().toString());
	    return true;
	case R.id.cancel:
	    closeEditor();
	    return true;
	default:
	    return super.onOptionsItemSelected(menuItem);
	}
    }

    private void showEditor(final String text) {
	if (galleryAdapter.getCurrentItem().isText()) {
	    galleryAdapter.getCurrentView().setVisibility(View.INVISIBLE);
	}
	descriptionEdit.setText(text);
	descriptionEdit.setVisibility(View.VISIBLE);
	galleryAdapter.prepareEdition();
	menu.getItem(0).setVisible(true);
	menu.getItem(1).setVisible(false);
	menu.getItem(2).setVisible(true);

	descriptionEdit.postDelayed(new Runnable() {

	    @Override
	    public void run() {
		descriptionEdit.dispatchTouchEvent(MotionEvent.obtain(
			SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
			MotionEvent.ACTION_DOWN, 0, 0, 0));
		descriptionEdit.dispatchTouchEvent(MotionEvent.obtain(
			SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
			MotionEvent.ACTION_UP, 0, 0, 0));
		descriptionEdit.setSelection(text.length());

	    }
	}, 1000);
    }

    private void closeEditor() {
	hideEditor();
	menu.getItem(0).setVisible(false);
	menu.getItem(1).setVisible(true);
	menu.getItem(2).setVisible(false);
	if (galleryAdapter.getCurrentItem().isText()) {
	    galleryAdapter.getCurrentView().setVisibility(View.VISIBLE);
	}
	galleryAdapter.finishEdition();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
	outState.putBoolean("fullscreen", !getActionBar().isShowing());
	outState.putInt("position", galleryAdapter.getCurrentPosition());
	if (descriptionEdit.getVisibility() == View.VISIBLE) {
	    outState.putString("description", descriptionEdit.getText()
		    .toString());
	} else {
	    outState.remove("description");
	}
	this.savedInstanceState = outState;
	super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
	ItemsListFragment.instance.setSelection(galleryAdapter
		.getCurrentPosition());
	super.onDestroy();
    }

}
