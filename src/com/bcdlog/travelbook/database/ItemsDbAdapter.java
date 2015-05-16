package com.bcdlog.travelbook.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.bcdlog.travelbook.FileStorage;
import com.bcdlog.travelbook.TBPreferences;
import com.bcdlog.travelbook.Utils;
import com.bcdlog.travelbook.activities.AndroidTravelBookActivity;
import com.bcdlog.travelbook.activities.items.ItemsListFragment;
import com.bcdlog.travelbook.network.Requester;

/**
 * @author bcdlog
 * 
 */
public class ItemsDbAdapter extends DbAdapter {

    public static final String KEY_CONTENT = "content";
    public static final String KEY_CONTENT_DATE = "content_date";
    public static final String KEY_CONTENT_TYPE = "content_type";
    public static final String KEY_DATE = "date";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_HEIGHT = "height";
    public static final String KEY_ID = "_id";
    public static final String KEY_LAST_MODIFIED = "last_modified";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LENGHT = "lenght";
    public static final String KEY_LIBRARY_PATH = "library_path";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_NAME = "name";
    public static final String KEY_PATH = "path";
    public static final String KEY_REFERENCE = "reference";
    public static final String KEY_STATUS = "status";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_WIDTH = "width";
    private static final String KEY_THUMBNAIL = "thumbnail";
    private static final String KEY_DOWNLOAD_ID = "download_id";

    public static enum Status {
	TO_BE_DOWNLOADED, CREATED, MODIFIED, DELETED
    };

    public ItemsDbAdapter(Context ctx) {
	super(ctx);
	listAll();
    }

    public static String getTableName() {
	return "Items";
    }

    public static String getTableCreateCommand() {
	return "create table " + getTableName() + " (" + KEY_ID
		+ " integer primary key autoincrement, " + KEY_CONTENT
		+ " text, " + KEY_CONTENT_DATE + " date, " + KEY_CONTENT_TYPE
		+ " text, " + KEY_DATE + " date, " + KEY_DURATION
		+ " integer, " + KEY_HEIGHT + " integer, " + KEY_LAST_MODIFIED
		+ " integer, " + KEY_LATITUDE + " double, " + KEY_LENGHT
		+ " integer, " + KEY_LIBRARY_PATH + " text, " + KEY_LONGITUDE
		+ " double, " + KEY_NAME + " text, " + KEY_PATH + " text, "
		+ KEY_REFERENCE + " text, " + KEY_STATUS + " text, "
		+ KEY_USER_ID + " integer, " + KEY_WIDTH + " integer,"
		+ KEY_THUMBNAIL + " blob, " + KEY_DOWNLOAD_ID + " integer)";
    }

    public void deleteItems(Long followerUserId) {
	Cursor cursor = fetchUserItems(followerUserId);
	try {
	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
		Item item = extractFrom(cursor);
		deleteItem(item);
		cursor.moveToNext();
	    }
	} finally {
	    if (cursor != null) {
		cursor.close();
	    }
	}
    }

    public void deleteItem(Long id) {
	Item item = fetchItem(id);
	if (item != null) {
	    deleteItem(item);
	    ItemsListFragment.instance.updateDisplay();
	}
    }

    private void deleteItem(Item item) {
	FileStorage.getInstance().deleteFiles(item);
	mDb.delete(getTableName(), KEY_ID + "=" + item.getId(), null);
    }

    public void deleteItem(String reference) {
	Item item = fetchItem(reference);
	if (item != null) {
	    deleteItem(item);
	    ItemsListFragment.instance.updateDisplay();
	}
    }

    public Cursor fetchAllItems() {
	return mDb.query(getTableName(), null, KEY_STATUS + " is null or "
		+ KEY_STATUS + " != ?",
		new String[] { FollowersDbAdapter.Status.DELETED.toString() },
		null, null, KEY_DATE + " desc");
    }

    public List<Item> getAllItems() {
	List<Item> items = new ArrayList<Item>();
	Cursor cursor = fetchAllItems();
	cursor.moveToFirst();
	while (!cursor.isAfterLast()) {
	    items.add(extractFrom(cursor));
	    cursor.moveToNext();
	}
	cursor.close();
	return items;
    }

    @Override
    public Item extractFrom(Cursor cursor) {
	Item item = new Item();
	item.setContent(cursor.getString(cursor.getColumnIndex(KEY_CONTENT)));
	item.setContentDate(Utils.parseDateFomDB(cursor.getString(cursor
		.getColumnIndex(KEY_CONTENT_DATE))));
	item.setContentType(cursor.getString(cursor
		.getColumnIndex(KEY_CONTENT_TYPE)));
	item.setDate(Utils.parseDateFomDB(cursor.getString(cursor
		.getColumnIndex(KEY_DATE))));
	item.setDuration(cursor.getString(cursor.getColumnIndex(KEY_DURATION)));
	item.setHeight(cursor.getLong(cursor.getColumnIndex(KEY_HEIGHT)));
	item.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
	item.setLastModified(cursor.getLong(cursor
		.getColumnIndex(KEY_LAST_MODIFIED)));
	item.setLatitude(cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)));
	item.setLength(cursor.getLong(cursor.getColumnIndex(KEY_LENGHT)));
	item.setLibraryPath(cursor.getString(cursor
		.getColumnIndex(KEY_LIBRARY_PATH)));
	item.setLongitude(cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE)));
	item.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
	item.setPath(cursor.getString(cursor.getColumnIndex(KEY_PATH)));
	item.setReference(cursor.getString(cursor.getColumnIndex(KEY_REFERENCE)));
	item.setStatus(cursor.getString(cursor.getColumnIndex(KEY_STATUS)));
	item.setUserId(cursor.getLong(cursor.getColumnIndex(KEY_USER_ID)));
	item.setWidth(cursor.getLong(cursor.getColumnIndex(KEY_WIDTH)));
	item.setThumbnail(cursor.getBlob(cursor.getColumnIndex(KEY_THUMBNAIL)));
	item.setDownloadId(cursor.getLong(cursor
		.getColumnIndex(KEY_DOWNLOAD_ID)));
	return item;
    }

    public Item createItem(JSONObject resourceModel) throws JSONException {
	createItem(contentValues(null, resourceModel));
	return fetchItem(resourceModel);
    }

    public Item updateItem(Item item, JSONObject resourceModel)
	    throws JSONException {
	ContentValues contentValues = contentValues(item, resourceModel);
	if (contentValues.size() > 0) {
	    mDb.update(getTableName(), contentValues,
		    KEY_ID + "=" + item.getId(), null);
	    ItemsListFragment.instance.updateDisplay();
	    return fetchItem(resourceModel);
	} else {
	    return item;
	}
    }

    public Item fetchItem(JSONObject resourceModel) throws JSONException {
	Item item = null;
	String userId = resourceModel.getString("userId");
	String resourceId = resourceModel.getString("id");
	String reference = resourceModel.getString("reference");
	Cursor cursor = mDb.query(getTableName(), null, KEY_USER_ID
		+ " = ? and (" + KEY_ID + " = ? or " + KEY_REFERENCE + " = ?)",
		new String[] { userId, resourceId, reference }, null, null,
		null);
	try {
	    if (cursor.getCount() > 1) {
		Utils.logError("Duplicate rows into Items for userId=" + userId
			+ " resourceId=" + resourceId + " reference="
			+ reference);
	    }
	    if (cursor.moveToFirst()) {
		item = extractFrom(cursor);
	    }
	} finally {
	    if (cursor != null) {
		cursor.close();
	    }
	}
	return item;
    }

    public Item fetchItem(String reference) {
	Item item = null;
	Cursor cursor = mDb.query(getTableName(), null, KEY_REFERENCE + " = ?",
		new String[] { reference }, null, null, null);
	try {
	    if (cursor.getCount() > 1) {
		Utils.logError("Duplicate rows into Items for reference="
			+ reference);
	    }
	    if (cursor.moveToFirst()) {
		item = extractFrom(cursor);
	    }
	} finally {
	    if (cursor != null) {
		cursor.close();
	    }
	}
	return item;
    }

    public Item fetchItem(long id) {
	Item item = null;
	Cursor cursor = mDb.query(getTableName(), null, KEY_ID + " = " + id,
		null, null, null, null);
	try {
	    if (cursor.getCount() > 1) {
		Utils.logError("Duplicate rows into Items for id=" + id);
	    }
	    if (cursor.moveToFirst()) {
		item = extractFrom(cursor);
	    }
	} finally {
	    if (cursor != null) {
		cursor.close();
	    }
	}
	return item;
    }

    public Item fetchItemFromDownloadId(long downloadId) {
	Item item = null;
	Cursor cursor = mDb.query(getTableName(), null, KEY_DOWNLOAD_ID + " = "
		+ downloadId, null, null, null, null);
	try {
	    if (cursor.moveToFirst()) {
		item = extractFrom(cursor);
	    }
	} finally {
	    if (cursor != null) {
		cursor.close();
	    }
	}
	return item;
    }

    private ContentValues contentValues(Item item) {
	ContentValues contentValues = new ContentValues();
	contentValues.put(KEY_CONTENT, item.getContent());
	contentValues.put(KEY_CONTENT_DATE,
		Utils.parseDateToDB(item.getContentDate()));
	contentValues.put(KEY_CONTENT_TYPE, item.getContentType());
	contentValues.put(KEY_DATE, Utils.parseDateToDB(item.getDate()));
	contentValues.put(KEY_DURATION, item.getDuration());
	contentValues.put(KEY_HEIGHT, item.getHeight());
	contentValues.put(KEY_ID, item.getId());
	contentValues.put(KEY_LAST_MODIFIED, item.getLastModified());
	contentValues.put(KEY_LATITUDE, item.getLatitude());
	contentValues.put(KEY_LENGHT, item.getLength());
	contentValues.put(KEY_LIBRARY_PATH, item.getLibraryPath());
	contentValues.put(KEY_LONGITUDE, item.getLongitude());
	contentValues.put(KEY_NAME, item.getName());
	contentValues.put(KEY_PATH, item.getPath());
	contentValues.put(KEY_REFERENCE, item.getReference());
	contentValues.put(KEY_STATUS, item.getStatus());
	contentValues.put(KEY_USER_ID, item.getUserId());
	contentValues.put(KEY_WIDTH, item.getWidth());
	if (item.getThumbnail() != null && item.getThumbnail().length > 0) {
	    contentValues.put(KEY_THUMBNAIL, item.getThumbnail());
	}
	contentValues.put(KEY_DOWNLOAD_ID, item.getDownloadId());
	return contentValues;
    }

    private ContentValues contentValues(Item item, JSONObject resourceModel)
	    throws JSONException {
	ContentValues contentValues = new ContentValues();
	JSONObject contentVersionModel = resourceModel
		.getJSONObject("contentVersionModel");

	contentValues.put(KEY_ID, resourceModel.getLong("id"));
	if (item != null && !item.isText() && item.getFile() == null) {
	    contentValues.put(KEY_STATUS, Status.TO_BE_DOWNLOADED.toString());
	}

	Date contentDate = Utils.parseDateFomJSON(contentVersionModel
		.getString("date"));
	if (item == null || item.getContentDate() == null
		|| item.getContentDate().before(contentDate)) {
	    contentValues.put(KEY_CONTENT, Utils
		    .replaceBRByCR(contentVersionModel.getString("content")));
	    contentValues.put(KEY_CONTENT_DATE,
		    Utils.parseDateToDB(contentDate));
	} else if (item != null && item.getContentDate() != null
		&& contentDate.before(item.getContentDate())) {
	    Requester.postItem(item);
	}

	String contentType = getStringFromJSON(resourceModel, "contentType");
	if (item == null || item.getContentType() == null
		|| contentType == null
		|| !contentType.equals(item.getContentType())) {
	    contentValues.put(KEY_CONTENT_TYPE, contentType);
	    if (item == null
		    && (contentType == null || !contentType.startsWith("text"))) {
		contentValues.put(KEY_STATUS,
			Status.TO_BE_DOWNLOADED.toString());
	    }
	}

	Date resourceDate = Utils.parseDateFomJSON(getStringFromJSON(
		resourceModel, "date"));
	if (resourceDate != null
		&& (item == null || item.getDate() == null || item.getDate()
			.before(resourceDate))) {
	    contentValues.put(KEY_DATE, Utils.parseDateToDB(resourceDate));
	}

	// No duration into Resource on server
	// contentValues.put(KEY_DURATION, item.getDuration());

	Long height = getLongFromJSON(resourceModel, "height");
	if (height != null
		&& (item == null || item.getHeight() == null || !height
			.equals(item.getHeight()))) {
	    contentValues.put(KEY_HEIGHT, height);
	}
	Long width = getLongFromJSON(resourceModel, "width");
	if (width != null
		&& (item == null || item.getWidth() == null || !width
			.equals(item.getWidth()))) {
	    contentValues.put(KEY_WIDTH, width);
	}

	Double latitude = getDoubleFromJSON(resourceModel, "latitude");
	if (latitude != null
		&& (item == null || item.getLatitude() == null || !latitude
			.equals(item.getLatitude()))) {
	    contentValues.put(KEY_LATITUDE, latitude);
	}
	Double longitude = getDoubleFromJSON(resourceModel, "longitude");
	if (longitude != null
		&& (item == null || item.getLongitude() == null || !longitude
			.equals(item.getLongitude()))) {
	    contentValues.put(KEY_LONGITUDE, longitude);
	}

	Long length = getLongFromJSON(resourceModel, "length");
	if (length != null
		&& (item == null || item.getLength() == null || !length
			.equals(item.getLength()))) {
	    contentValues.put(KEY_LENGHT, length);
	} else if (item != null && !contentType.startsWith("text")
		&& (item.getFile() == null || item.isIncomplete())) {
	    // Recovery
	    contentValues.put(KEY_STATUS, Status.TO_BE_DOWNLOADED.toString());
	}

	String reference = resourceModel.getString("reference");
	if (reference != null
		&& (item == null || item.getReference() == null || !reference
			.equals(item.getReference()))) {
	    contentValues.put(KEY_REFERENCE, reference);
	    contentValues.put(KEY_NAME,
		    reference.substring(reference.lastIndexOf("/") + 1));
	}

	Long userId = resourceModel.getLong("userId");
	if (userId != null
		&& (item == null || item.getUserId() == null || !userId
			.equals(item.getUserId()))) {
	    contentValues.put(KEY_USER_ID, userId);
	}

	Long lastModified = resourceModel.getLong("lastModified");
	if (lastModified != null
		&& (item == null || item.getLastModified() == null || lastModified > item
			.getLastModified())) {
	    contentValues.put(KEY_LAST_MODIFIED, lastModified);
	    if (!contentType.startsWith("text")) {
		contentValues.put(KEY_STATUS,
			Status.TO_BE_DOWNLOADED.toString());
		if (item != null) {
		    // Delete old item
		    FileStorage.getInstance().deleteFiles(item);
		}
	    }
	}

	return contentValues;
    }

    public void createItem(Item item) {
	createItem(contentValues(item));
    }

    private void createItem(ContentValues contentValues) {
	if (contentValues.size() > 0) {
	    mDb.insert(getTableName(), null, contentValues);
	    ItemsListFragment.instance.updateDisplay();
	}
    }

    public void updateItem(Item item) {
	mDb.update(getTableName(), contentValues(item),
		KEY_ID + "=" + item.getId(), null);
	ItemsListFragment.instance.updateDisplay();
    }

    public void updateItemId(Item item) {
	if (!item.isText() && item.getPath() != null) {
	    FileStorage.getInstance().moveItemToId(item);
	}
	mDb.update(getTableName(), contentValues(item), KEY_REFERENCE + "='"
		+ item.getReference() + "'", null);
	ItemsListFragment.instance.updateDisplay();
    }

    public Cursor fetchUserItems(Long userId) {
	return mDb.query(
		getTableName(),
		null,
		"(" + KEY_STATUS + " is null or " + KEY_STATUS + " = ?  or "
			+ KEY_STATUS + " = ?) and " + KEY_USER_ID + " = ?",
		new String[] { ItemsDbAdapter.Status.CREATED.toString(),
			ItemsDbAdapter.Status.MODIFIED.toString(),
			String.valueOf(userId) }, null, null, KEY_DATE
			+ " DESC");
    }

    public Item createItem(String contentType, String extension, Date date) {
	Item item = new Item();
	item.setUserId(TBPreferences.getUserId());
	item.setLastModified(date.getTime());
	item.setContentType(contentType);
	item.setDate(date);
	item.setName(Utils.parseDateToJSON(date) + extension);
	item.setReference("/Album/" + TBPreferences.getUserNickname() + "/"
		+ item.getName());
	return item;
    }

    public void listAll() {
	super.listAll(getTableName());
    }

    @Override
    protected void recreateTable() {
	mDb.execSQL("drop table " + getTableName());
	mDb.execSQL(getTableCreateCommand());
	AndroidTravelBookActivity.instance.refresh();
    }

    public Cursor fetchItemsToBeDeleted() {
	return mDb.query(getTableName(), new String[] { KEY_ID }, KEY_STATUS
		+ " = ?",
		new String[] { FollowersDbAdapter.Status.DELETED.toString() },
		null, null, null);
    }

    public int getPosition(Item alreadyAddedItem) {
	Cursor cursor = fetchUserItems(TBPreferences.getUserId());
	try {
	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
		String reference = cursor.getString(cursor
			.getColumnIndex(KEY_REFERENCE));
		if (reference != null
			&& reference.equals(alreadyAddedItem.getReference())) {
		    return cursor.getPosition();
		}
		cursor.moveToNext();
	    }
	} finally {
	    if (cursor != null) {
		cursor.close();
	    }
	}
	return -1;
    }
}
