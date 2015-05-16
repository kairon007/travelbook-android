package com.bcdlog.travelbook;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * User preferences
 * 
 * @author bcdlog
 * 
 */
public class TBPreferences {

    static private SharedPreferences prefs;

    public static void init(Context context) {
	if (prefs == null) {
	    prefs = context.getSharedPreferences("TravelBookPreferences",
		    Context.MODE_PRIVATE);
	}
    }

    public static String getToken() {
	return prefs.getString("userToken", null);
    }

    public static void setUserPreferences(JSONObject userModel)
	    throws JSONException {
	SharedPreferences.Editor editor = prefs.edit();
	editor.putString("userLogin", userModel.getString("login"));
	editor.putString("userId", userModel.getString("id"));
	editor.putString("userToken", userModel.getString("token"));
	editor.putString("userNickname", userModel.getString("nickname"));
	editor.putString("userDirectoryNickname",
		userModel.getString("directoryNickname"));
	String receptionAlbumId = userModel.getString("receptionAlbumId");
	JSONObject albumModels = userModel.getJSONObject("albumModels");
	JSONObject albumModel = albumModels.getJSONObject(receptionAlbumId);
	editor.putString("receptionAlbumName", albumModel.getString("name"));
	editor.putString("receptionAlbumKind", albumModel.getString("kind"));
	JSONObject contentVersionModel = albumModel
		.getJSONObject("contentVersionModel");
	editor.putString("receptionAlbumDescription",
		contentVersionModel.getString("content"));
	editor.commit();
    }

    public static Long getUserId() {
	try {
	    return Long.parseLong(prefs.getString("userId", null));
	} catch (Throwable t) {
	    return null;
	}
    }

    public static void setToken(String token) {
	SharedPreferences.Editor editor = prefs.edit();
	editor.putString("userToken", token);
	editor.commit();
    }

    public static String getUserNickname() {
	return prefs.getString("userNickname", "item");
    }

    public static String getReceptionAlbumKind() {
	return prefs.getString("receptionAlbumKind", null);
    }

    public static String getReceptionAlbumName() {
	return prefs.getString("receptionAlbumName", "");
    }

    public static String getReceptionAlbumDescription() {
	return prefs.getString("receptionAlbumDescription", "");
    }

    public static void setReceptionAlbumKind(String kind) {
	SharedPreferences.Editor editor = prefs.edit();
	editor.putString("receptionAlbumKind", kind);
	editor.commit();
    }

    public static void clear() {
	SharedPreferences.Editor editor = prefs.edit();
	editor.clear();
	editor.commit();
    }
}
