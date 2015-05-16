package com.bcdlog.travelbook.activities.gallery;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bcdlog.travelbook.R;
import com.bcdlog.travelbook.Utils;
import com.bcdlog.travelbook.activities.AndroidTravelBookActivity;
import com.bcdlog.travelbook.database.Item;
import com.bcdlog.travelbook.database.ItemsDbAdapter;

public class GalleryAdapter extends BaseAdapter implements
	OnItemSelectedListener {

    int mGalleryItemBackground;
    private final GalleryActivity galleryActivity;
    private final List<Item> items;
    private int currentPosition;
    private Item currentItem;
    private View currentView;
    private final LayoutInflater layoutInflater;
    private View[] views;

    public GalleryAdapter(GalleryActivity galleryActivity, long userId) {
	this.galleryActivity = galleryActivity;
	TypedArray attr = galleryActivity
		.obtainStyledAttributes(R.styleable.GalleryActivity);
	mGalleryItemBackground = attr.getResourceId(
		R.styleable.GalleryActivity_android_galleryItemBackground, 0);
	attr.recycle();

	ItemsDbAdapter itemsDbAdapter = AndroidTravelBookActivity.instance
		.getItemsDbAdapter();
	items = new ArrayList<Item>();
	Cursor cursor = itemsDbAdapter.fetchUserItems(userId);
	cursor.moveToFirst();
	while (!cursor.isAfterLast()) {
	    items.add(itemsDbAdapter.extractFrom(cursor));
	    cursor.moveToNext();
	}
	cursor.close();
	layoutInflater = LayoutInflater.from(galleryActivity);
	if (items.isEmpty()) {
	    galleryActivity.finish();
	} else {
	    views = new View[items.size()];
	}
    }

    @Override
    public int getCount() {
	return items.size();
    }

    @Override
    public Object getItem(int position) {
	return position;
    }

    @Override
    public long getItemId(int position) {
	return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	// Recycling
	int previous = currentPosition - 2;
	if (previous >= 0 && views[previous] != null) {
	    recycle(previous);
	}
	int next = currentPosition + 2;
	if (next < items.size() && views[next] != null) {
	    recycle(next);
	}
	View view = views[position];
	if (view != null) {
	    return view;
	} else {
	    try {
		Item item = null;
		if (items != null && position < items.size()) {
		    item = items.get(position);
		}
		if (item == null) {
		    Utils.logError("getView : No item at position " + position);
		    galleryActivity.finish();
		    return null;
		}
		Utils.logInfo("Create view " + position);
		switch (item.getType()) {
		case Item.TEXT:
		    view = textView(item, convertView, parent);
		    break;
		case Item.IMAGE:
		    view = imageView(item, convertView, parent);
		    break;
		case Item.VIDEO:
		    view = videoView(item, convertView, parent);
		    break;
		case Item.SOUND:
		    view = audioView(item, convertView, parent);
		    break;
		default:
		    view = unknownView(item, convertView, parent);
		    break;
		}
		fillTextView(item, view);
	    } catch (Throwable t) {
		Utils.logError(t);
		view = waitingView();
	    }
	    view.setBackgroundColor(Color.BLACK);
	    view.setLayoutParams(new Gallery.LayoutParams(
		    LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	    views[position] = view;
	    return view;
	}
    }

    /**
     * Do not recycle video because image is used by items list
     * 
     * @param position
     */
    private void recycle(int position) {
	Item item = items.get(position);
	if (item != null && item.isImage()) {
	    View view = views[position];
	    ImageView imageView = (ImageView) view.findViewById(R.id.image);
	    if (imageView != null) {
		((BitmapDrawable) imageView.getDrawable()).getBitmap()
			.recycle();
		views[position] = null;
	    }
	}
    }

    private View waitingView() {
	ProgressBar spinner = new ProgressBar(galleryActivity);
	return spinner;
    }

    private View unknownView(Item item, View convertView, ViewGroup parent) {
	View view = convertView;
	if (view == null) {
	    view = layoutInflater.inflate(R.layout.image, null);
	}
	ImageView imageView = (ImageView) view.findViewById(R.id.image);
	Utils.createIcon(item, imageView);
	return view;
    }

    private View audioView(final Item item, View convertView, ViewGroup parent) {
	View view = convertView;
	if (view == null) {
	    LayoutInflater layoutInflater = LayoutInflater
		    .from(galleryActivity);
	    view = layoutInflater.inflate(R.layout.audio, null);
	}
	final File file = item.getFile();
	if (file != null) {
	    ImageView imageView = (ImageView) view.findViewById(R.id.image);
	    imageView.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
		    Intent intent = new Intent(galleryActivity,
			    AudioPlayerActivity.class);
		    Bundle bundle = new Bundle();
		    bundle.putString("path", file.getAbsolutePath());
		    bundle.putString("description", item.getContent());
		    intent.putExtras(bundle);
		    galleryActivity.startActivity(intent);
		}
	    });
	    Utils.createIcon(item, imageView);
	} else {
	    Utils.logError("Item has no file : " + item.getName());
	}
	TextView textView = (TextView) view.findViewById(R.id.name);
	textView.setText(item.getName());
	return view;
    }

    private View videoView(final Item item, View convertView, ViewGroup parent) {
	View view = convertView;
	if (view == null) {
	    view = layoutInflater.inflate(R.layout.video, null);
	}
	File file = item.getFile();
	if (file != null) {
	    try {
		ImageView imageView = (ImageView) view.findViewById(R.id.image);
		imageView.setOnClickListener(new OnClickListener() {

		    @Override
		    public void onClick(View v) {
			Intent intent = new Intent(galleryActivity,
				VideoPlayerActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("path", item.getFile()
				.getAbsolutePath());
			intent.putExtras(bundle);
			galleryActivity.startActivity(intent);
		    }
		});
		Utils.createIcon(item, imageView);
	    } catch (Exception e) {
		Utils.logError(e);
	    }
	}
	return view;
    }

    private View imageView(Item item, View convertView, ViewGroup parent)
	    throws FileNotFoundException {
	View view = convertView;
	if (view == null) {
	    view = layoutInflater.inflate(R.layout.image, null);
	}
	File file = item.getFile();
	if (file != null) {
	    ImageView imageView = (ImageView) view.findViewById(R.id.image);
	    // // Get the dimensions of the View
	    int targetW = Utils.getScreenWidth();
	    int targetH = Utils.getScreenHeight();

	    // Get the dimensions of the bitmap
	    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
	    bmOptions.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(file.getAbsolutePath(), bmOptions);
	    int photoW = bmOptions.outWidth;
	    int photoH = bmOptions.outHeight;

	    // Determine how much to scale down the image
	    int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

	    // Decode the image file into a Bitmap sized to fill the View
	    bmOptions.inJustDecodeBounds = false;
	    bmOptions.inSampleSize = scaleFactor;
	    bmOptions.inPurgeable = true;
	    final Bitmap bitmap = BitmapFactory.decodeFile(
		    file.getAbsolutePath(), bmOptions);
	    imageView.setImageBitmap(bitmap);

	    // OutOfMemory
	    // imageView.setImageBitmap(BitmapFactory.decodeFile(file
	    // .getAbsolutePath()));
	}
	return view;
    }

    private View textView(Item item, View convertView, ViewGroup parent) {
	View view = convertView;
	if (view == null) {
	    view = layoutInflater.inflate(R.layout.text, null);
	}
	return view;
    }

    private void fillTextView(Item item, View view) {
	TextView textView = (TextView) view.findViewById(R.id.text);
	String content = item.getContent();
	if (content != null) {
	    textView.setText(content);
	} else {
	    textView.setText("");
	}
	textView.setMovementMethod(new ScrollingMovementMethod());
	if (item.isImage() || item.isVideo()) {
	    // Use available height to display text
	    textView.setHeight(galleryActivity.getWindowManager()
		    .getDefaultDisplay().getHeight() / 4);
	} else if (item.isAudio()) {
	    textView.setHeight(galleryActivity.getWindowManager()
		    .getDefaultDisplay().getHeight() / 2);
	}

    }

    @Override
    public int getViewTypeCount() {
	// Text, image, video, sound and unknown
	return 5;
    }

    @Override
    public int getItemViewType(int position) {
	return (items.get(position)).getType();
    }

    public void saveDescription(String content) {
	AndroidTravelBookActivity.instance.saveDescription(currentItem,
		content, new Date());
	if (currentView != null) {
	    TextView textView = (TextView) currentView.findViewById(R.id.text);
	    textView.setText(content);
	}
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
	    long id) {
	if (view == null) {
	    return;
	}
	if (items == null || position >= items.size()) {
	    galleryActivity.finish();
	    return;
	}
	currentItem = items.get(position);
	if (currentItem == null) {
	    Utils.logError("onItemSelected : No item at position " + position);
	    return;
	}
	currentView = view;
	currentPosition = position;
	changeTextVisibility(galleryActivity.getActionBar().isShowing());

    }

    void changeTextVisibility(boolean visible) {
	if (currentView != null
		&& (currentItem.isImage() || currentItem.isVideo())) {
	    TextView textView = (TextView) currentView.findViewById(R.id.text);
	    if (textView != null) {
		if (visible) {
		    textView.setVisibility(View.VISIBLE);
		} else {
		    textView.setVisibility(View.INVISIBLE);
		}
	    }
	}
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    /**
     * @return the currentPosition
     */
    public int getCurrentPosition() {
	return currentPosition;
    }

    /**
     * @return the currentItem
     */
    public Item getCurrentItem() {
	return currentItem;
    }

    public View getCurrentView() {
	return currentView;
    }

    public void prepareEdition() {
	if (currentView != null) {
	    TextView textView = (TextView) currentView.findViewById(R.id.text);
	    textView.setVisibility(View.INVISIBLE);
	}
    }

    public void finishEdition() {
	if (currentView != null) {
	    TextView textView = (TextView) currentView.findViewById(R.id.text);
	    textView.setVisibility(View.VISIBLE);
	}
    }

}
