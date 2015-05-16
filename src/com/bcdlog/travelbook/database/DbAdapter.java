package com.bcdlog.travelbook.database;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bcdlog.travelbook.Utils;

abstract public class DbAdapter {

    private static final String DATABASE_NAME = "database";
    private static final int DATABASE_VERSION = 1;

    private DatabaseHelper mDbHelper;
    private final Context mCtx;

    protected SQLiteDatabase mDb;

    abstract protected Bean extractFrom(Cursor cursor);

    abstract protected void recreateTable();

    /**
     * Common part
     * 
     * @author bcdlog
     * 
     */
    private class DatabaseHelper extends SQLiteOpenHelper {

	DatabaseHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	    db.execSQL(ItemsDbAdapter.getTableCreateCommand());
	    db.execSQL(FollowersDbAdapter.getTableCreateCommand());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Utils.logError("Upgrading database from version " + oldVersion
		    + " to " + newVersion + ", which will destroy all old data");
	    db.execSQL("DROP TABLE IF EXISTS " + ItemsDbAdapter.getTableName());
	    db.execSQL("DROP TABLE IF EXISTS "
		    + FollowersDbAdapter.getTableName());
	    onCreate(db);
	}
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx
     *            the Context within which to work
     */
    public DbAdapter(Context ctx) {
	this.mCtx = ctx;
	open();
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException
     *             if the database could be neither opened or created
     */
    public void open() throws SQLException {
	mDbHelper = new DatabaseHelper(mCtx);
	mDb = mDbHelper.getWritableDatabase();
    }

    protected Long getLongFromJSON(JSONObject followerModel, String field) {
	try {
	    return followerModel.getLong(field);
	} catch (JSONException e) {
	}
	return null;
    }

    protected Double getDoubleFromJSON(JSONObject followerModel, String field) {
	try {
	    return followerModel.getDouble(field);
	} catch (JSONException e) {
	}
	return null;
    }

    protected String getStringFromJSON(JSONObject followerModel, String field) {
	try {
	    return followerModel.getString(field);
	} catch (JSONException e) {
	}
	return null;
    }

    public void close() {
	mDbHelper.close();
    }

    public void listAll(String tableName) {
	try {
	    Cursor cursor = mDb.query(tableName, null, null, null, null, null,
		    null);
	    cursor.moveToFirst();
	    Utils.logInfo(tableName + " table contains " + cursor.getCount()
		    + " rows :");
	    while (!cursor.isAfterLast()) {
		Utils.logInfo(extractFrom(cursor).toString());
		cursor.moveToNext();
	    }
	    cursor.close();
	} catch (Throwable t) {
	    Utils.logError(t);
	    Utils.logError("Resetting table " + tableName);
	    recreateTable();
	}
    }

}
