package com.bcdlog.travelbook.database;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import com.bcdlog.travelbook.Utils;
import com.bcdlog.travelbook.activities.AndroidTravelBookActivity;
import com.bcdlog.travelbook.activities.followers.FollowersListFragment;

/**
 * @author bcdlog
 * 
 */
public class FollowersDbAdapter extends DbAdapter {

    public static final String KEY_ALIASES = "aliases";
    public static final String KEY_CONTACT = "contact";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_ID = "_id";
    public static final String KEY_ITEMS = "items";
    public static final String KEY_NICKNAME = "nickname";
    public static final String KEY_STATUS = "status";
    public static final String KEY_USER_ID = "userId";

    public static enum Status {
	DELETED, LOCALLY_ADDED
    };

    public FollowersDbAdapter(Context ctx) {
	super(ctx);
	listAll();
    }

    public static String getTableName() {
	return "Followers";
    }

    public static String getTableCreateCommand() {
	return "create table " + getTableName() + " (" + KEY_ID
		+ " integer primary key autoincrement, " + KEY_ALIASES
		+ " text, " + KEY_CONTACT + " text, " + KEY_EMAIL + " text, "
		+ KEY_ITEMS + " integer, " + KEY_NICKNAME + " text, "
		+ KEY_STATUS + " text, " + KEY_USER_ID + " integer);";
    }

    /**
     * Delete the note with the given id
     * 
     * @param id
     *            to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteFollower(long id) {
	boolean result = mDb.delete(getTableName(), KEY_ID + "=" + id, null) > 0;
	if (result && FollowersListFragment.instance != null) {
	    FollowersListFragment.instance.updateDisplay();
	}
	return result;
    }

    /**
     * Delete followers marked as deleted
     */
    public Cursor fetchFollowersToBeDeleted() {
	return mDb.query(getTableName(), new String[] { KEY_ID }, KEY_STATUS
		+ " = ?",
		new String[] { FollowersDbAdapter.Status.DELETED.toString() },
		null, null, null);
    }

    public Follower fetchFollower(JSONObject followerModel)
	    throws JSONException {
	String email = followerModel.getString("login");
	String id = followerModel.getString("id");
	Follower follower = null;
	Cursor cursor = mDb
		.query(getTableName(), null, KEY_EMAIL + " = ? or " + KEY_ID
			+ " = ?", new String[] { email, id }, null, null, null);
	try {
	    cursor.moveToFirst();
	    if (cursor.getCount() > 1) {
		Utils.logError("Duplicate rows into Followers for email="
			+ email + " id=" + id);
	    }
	    if (!cursor.isAfterLast()) {
		follower = extractFrom(cursor);
	    }
	} finally {
	    if (cursor != null) {
		cursor.close();
	    }
	}
	return follower;

    }

    public Follower fetchFollower(String email, String contact) {
	Cursor cursor = mDb.query(getTableName(), null, KEY_CONTACT + " = '"
		+ contact + "' or " + KEY_EMAIL + " = '" + email + "' or "
		+ KEY_ALIASES + " = '" + email + "' or " + KEY_ALIASES
		+ " like '" + email + ",%' or " + KEY_ALIASES + " like '%,"
		+ email + ",%' or " + KEY_ALIASES + " like '%," + email + "' ",
		null, null, null, null);
	Follower follower = null;
	try {
	    if (cursor.getCount() > 1) {
		Utils.logError("Duplicate rows into Followers for email="
			+ email + " contact=" + contact);
	    }
	    if (cursor.moveToFirst()) {
		follower = extractFrom(cursor);
	    }
	} finally {
	    if (cursor != null) {
		cursor.close();
	    }
	}
	return follower;

    }

    public Follower fetchFollower(long id) {
	Follower follower = null;
	Cursor cursor = mDb.query(getTableName(), null, KEY_ID + " = " + id,
		null, null, null, null);
	try {
	    if (cursor.getCount() > 1) {
		Utils.logError("Duplicate rows into Followers for id=" + id);
	    }
	    if (cursor.moveToFirst()) {
		follower = extractFrom(cursor);
	    }
	} finally {
	    if (cursor != null) {
		cursor.close();
	    }
	}
	return follower;
    }

    @Override
    public Follower extractFrom(Cursor cursor) {
	Follower follower = new Follower();
	follower.setAliases(cursor.getString(cursor.getColumnIndex(KEY_ALIASES)));
	follower.setContact(cursor.getString(cursor.getColumnIndex(KEY_CONTACT)));
	follower.setEmail(cursor.getString(cursor.getColumnIndex(KEY_EMAIL)));
	follower.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
	follower.setItems(cursor.getLong(cursor.getColumnIndex(KEY_ITEMS)));
	follower.setNickname(cursor.getString(cursor
		.getColumnIndex(KEY_NICKNAME)));
	follower.setStatus(cursor.getString(cursor.getColumnIndex(KEY_STATUS)));
	follower.setUserId(cursor.getLong(cursor.getColumnIndex(KEY_USER_ID)));
	return follower;
    }

    public Follower createFollower(JSONObject followerModel)
	    throws JSONException {
	createFollower(contentValues(null, followerModel));
	return fetchFollower(followerModel);
    }

    public Follower updateFollower(Follower follower, JSONObject followerModel)
	    throws JSONException {
	ContentValues contentValues = contentValues(follower, followerModel);
	if (contentValues.size() > 0) {
	    mDb.update(getTableName(), contentValues,
		    KEY_ID + "=" + follower.getId(), null);
	    if (FollowersListFragment.instance != null) {
		FollowersListFragment.instance.updateDisplay();
	    }
	    return fetchFollower(followerModel);
	} else {
	    return follower;
	}
    }

    public void createFollower(Follower follower) {
	createFollower(contentValues(follower));
    }

    private void createFollower(ContentValues contentValues) {
	mDb.insert(getTableName(), null, contentValues);
	if (FollowersListFragment.instance != null) {
	    FollowersListFragment.instance.updateDisplay();
	}
    }

    public void updateFollower(Follower follower) {
	mDb.update(getTableName(), contentValues(follower), KEY_ID + "="
		+ follower.getId(), null);
	if (FollowersListFragment.instance != null) {
	    FollowersListFragment.instance.updateDisplay();
	}
    }

    private ContentValues contentValues(Follower follower) {
	ContentValues contentValues = new ContentValues();
	contentValues.put(KEY_ALIASES, follower.getAliases());
	contentValues.put(KEY_CONTACT, follower.getContact());
	contentValues.put(KEY_EMAIL, follower.getEmail());
	contentValues.put(KEY_ID, follower.getId());
	contentValues.put(KEY_ITEMS, follower.getItems());
	contentValues.put(KEY_NICKNAME, follower.getNickname());
	contentValues.put(KEY_STATUS, follower.getStatus());
	contentValues.put(KEY_USER_ID, follower.getUserId());
	return contentValues;
    }

    private ContentValues contentValues(Follower follower,
	    JSONObject followerModel) throws JSONException {
	ContentValues contentValues = new ContentValues();

	contentValues.put(KEY_ID, followerModel.getLong("id"));

	String aliases = getStringFromJSON(followerModel, "aliases");
	if (follower == null || follower.getAliases() == null
		|| aliases == null || !follower.getAliases().equals(aliases)) {
	    contentValues.put(KEY_ALIASES, aliases);
	}

	String email = followerModel.getString("login");
	if (follower == null || follower.getEmail() == null
		|| !follower.getEmail().equals(email)) {
	    contentValues.put(KEY_EMAIL, email);
	}

	Long items = getLongFromJSON(followerModel, "items");
	if (follower == null || follower.getItems() == null || items == null
		|| !follower.getItems().equals(items)) {
	    contentValues.put(KEY_ITEMS, items);
	}

	String nickname = getStringFromJSON(followerModel, "nickname");
	if (follower == null || follower.getNickname() == null
		|| !follower.getNickname().equals(nickname)) {
	    contentValues.put(KEY_NICKNAME, nickname);
	}

	Long userId = getLongFromJSON(followerModel, "userId");
	if (follower == null || follower.getUserId() == null || userId == null
		|| !follower.getUserId().equals(userId)) {
	    contentValues.put(KEY_USER_ID, userId);
	}
	return contentValues;
    }

    public Cursor fetchFollowers() {
	return mDb.query(getTableName(), null, KEY_STATUS + " is null or "
		+ KEY_STATUS + " != ?",
		new String[] { FollowersDbAdapter.Status.DELETED.toString() },
		null, null, KEY_CONTACT + "," + KEY_NICKNAME + "," + KEY_EMAIL);
    }

    public List<Follower> getAllFollowers() {
	List<Follower> followers = new ArrayList<Follower>();
	Cursor cursor = fetchFollowers();
	cursor.moveToFirst();
	while (!cursor.isAfterLast()) {
	    followers.add(extractFrom(cursor));
	    cursor.moveToNext();
	}
	cursor.close();
	return followers;
    }

    public static Loader<Cursor> createListCursorLoader(Context context) {
	return new CursorLoader(context, Uri.EMPTY,
		new String[] { KEY_CONTACT }, KEY_STATUS + " != ?",
		new String[] { FollowersDbAdapter.Status.DELETED.toString() },
		" order by " + KEY_CONTACT);
    }

    public void listAll() {
	super.listAll(getTableName());
    }

    @Override
    protected void recreateTable() {
	mDb.execSQL("delete table " + getTableName());
	mDb.execSQL(getTableCreateCommand());
	AndroidTravelBookActivity.instance.refresh();
    }

}
