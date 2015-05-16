package com.bcdlog.travelbook.activities.items;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bcdlog.travelbook.R;
import com.bcdlog.travelbook.TBPreferences;
import com.bcdlog.travelbook.Utils;
import com.bcdlog.travelbook.activities.AndroidTravelBookActivity;
import com.bcdlog.travelbook.activities.TBListFragment;
import com.bcdlog.travelbook.database.Bean;
import com.bcdlog.travelbook.database.Item;
import com.bcdlog.travelbook.database.ItemsDbAdapter;
import com.bcdlog.travelbook.network.Requester;

public class ItemsListFragment extends TBListFragment {

    public static ItemsListFragment instance;

    private ItemsDbAdapter itemsDbAdapter;

    private Integer currentPosition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
	View view = inflater.inflate(R.layout.items_list, container, false);
	instance = this;
	init(view);
	itemsDbAdapter = AndroidTravelBookActivity.instance.getItemsDbAdapter();
	fillData();
	AndroidTravelBookActivity.instance.startupRefresh();
	return view;
    }

    @Override
    protected int fillData() {
	Cursor cursor = itemsDbAdapter
		.fetchUserItems(TBPreferences.getUserId());
	getActivity().startManagingCursor(cursor);
	mAdapter = new ItemCursorAdapter(this, cursor);
	setListAdapter(mAdapter);
	if (currentPosition != null) {
	    setSelection(currentPosition);
	    currentPosition = null;
	}
	return cursor.getCount();
    }

    public void pickMedia(Uri uri) {
	// MEDIA GALLERY
	String filePath = getPath(uri);
	if (filePath == null) {
	    // OI FILE Manager
	    filePath = uri.getPath();
	}
	if (filePath == null) {
	    Utils.logError("Failed to get file path from uri " + uri);
	    return;
	}

	// MediaMetadataRetriever mediaMetadataRetriever = new
	// MediaMetadataRetriever();
	// mediaMetadataRetriever.setDataSource(getActivity(), uri);
	// String mimeType = mediaMetadataRetriever
	// .extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
	// String mdate = mediaMetadataRetriever
	// .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);

	Date date = new Date();
	String mimeType = null;
	try {
	    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
	    mediaMetadataRetriever.setDataSource(getActivity(), uri);
	    mimeType = mediaMetadataRetriever
		    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
	} catch (Throwable t) {
	    Utils.logError(t);
	}
	if (mimeType == null) {
	    mimeType = Utils.mimeTypeFromFilname(filePath.substring(filePath
		    .lastIndexOf("/") + 1));
	}
	if (mimeType.equals("image/jpeg")) {
	    try {
		ExifInterface exifInterface = new ExifInterface(filePath);
		String originalDate = exifInterface
			.getAttribute(ExifInterface.TAG_DATETIME);
		if (originalDate != null) {
		    SimpleDateFormat sdtf = new SimpleDateFormat(
			    "yyyy:MM:dd HH:mm:ss");
		    date = sdtf.parse(originalDate);
		} else {
		    Utils.logInfo(exifInterface.toString());
		}
	    } catch (Throwable t) {
		Utils.logError(t);
	    }
	}

	Item item = itemsDbAdapter.createItem(mimeType,
		Utils.extractFileExtension(filePath), date);
	try {
	    String name = filePath.substring(filePath.lastIndexOf("/") + 1);
	    item.setName(name);
	    item.setReference("/Album/" + TBPreferences.getUserNickname() + "/"
		    + item.getName());
	} catch (Exception e) {
	    Utils.logError(e);
	}
	saveMediaItem(item, filePath, date);
    }

    private void saveMediaItem(Item item, String filePath, Date date) {
	Item alreadyAddedItem = itemsDbAdapter.fetchItem(item.getReference());
	if (alreadyAddedItem != null) {
	    alreadyAddedItem.setPath(null);
	    alreadyAddedItem.setLibraryPath(filePath);
	    alreadyAddedItem.setModified();
	    Utils.createThumbnail(alreadyAddedItem);
	    final int position = itemsDbAdapter.getPosition(alreadyAddedItem);
	    if (position >= 0) {
		currentPosition = position;
	    }

	    itemsDbAdapter.updateItem(alreadyAddedItem);

	    Requester.postItem(item);
	} else {
	    item.setContent("");
	    item.setContentDate(date);
	    item.setLibraryPath(filePath);
	    item.setStatus(ItemsDbAdapter.Status.CREATED.toString());
	    Utils.createThumbnail(item);
	    itemsDbAdapter.createItem(item);
	    Requester.postItem(item);
	}
    }

    public void addMediaItem(Item item, Intent intent) {
	if (intent != null) {
	    Uri uri = intent.getData();
	    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
	    mediaMetadataRetriever.setDataSource(getActivity(), uri);
	    String mimeType = mediaMetadataRetriever
		    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
	    item.setContentType(mimeType);
	}

	item.setContent("");
	item.setContentDate(item.getDate());
	item.setStatus(ItemsDbAdapter.Status.CREATED.toString());
	Utils.createThumbnail(item);
	itemsDbAdapter.createItem(item);
	Requester.postItem(item);
    }

    public String getPath(Uri uri) {
	String[] projection = { MediaStore.Images.Media.DATA };
	Cursor cursor = getActivity().managedQuery(uri, projection, null, null,
		null);
	if (cursor != null) {
	    int column_index = cursor
		    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
	} else
	    return null;
    }

    @Override
    public void delete(Bean bean) {
	Item item = (Item) bean;
	if (item.isCreated()) {
	    itemsDbAdapter.deleteItem(item.getId());
	} else {
	    item.setStatus(ItemsDbAdapter.Status.DELETED.toString());
	    itemsDbAdapter.updateItem(item);
	    Requester.deleteItem(item.getId());
	}
	fillData();
    }

}
