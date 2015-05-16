package com.bcdlog.travelbook.network;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;

import com.bcdlog.travelbook.TBPreferences;
import com.bcdlog.travelbook.Utils;
import com.bcdlog.travelbook.activities.AndroidTravelBookActivity;
import com.bcdlog.travelbook.activities.items.ItemsListFragment;
import com.bcdlog.travelbook.database.Follower;
import com.bcdlog.travelbook.database.FollowersDbAdapter;
import com.bcdlog.travelbook.database.Item;
import com.bcdlog.travelbook.database.ItemsDbAdapter;

public class Synchronizer {

    private FollowersDbAdapter followersDbAdater;
    List<Follower> receivedFollowers;

    private ItemsDbAdapter itemsDbAdater;
    List<Item> receivedItems;

    public void synchronize(JSONObject userAndItemsModel) {
	try {
	    ItemsListFragment.instance.showProgressBar();

	    JSONObject userModel = userAndItemsModel.getJSONObject("userModel");
	    TBPreferences.setUserPreferences(userModel);

	    JSONObject albumContextModel = userAndItemsModel
		    .getJSONObject("albumContextModel");
	    syncFollowers(albumContextModel.getJSONArray("followerModels"));
	    syncItems(albumContextModel.getJSONArray("resourceModels"));
	} catch (Throwable t) {
	    Utils.logError(t);
	}
    }

    private void syncItems(JSONArray resourceModels) throws JSONException {
	itemsDbAdater = AndroidTravelBookActivity.instance.getItemsDbAdapter();
	receivedItems = new ArrayList<Item>();

	// Try to delete items asked to be deleted
	List<Long> deletedItems = deleteItems();

	// Create or update items from the server
	for (int i = 0; i < resourceModels.length(); ++i) {
	    JSONObject resourceModel = (JSONObject) resourceModels.get(i);
	    Long resourceId = resourceModel.getLong("id");
	    if (deletedItems.contains(resourceId)) {
		Requester.deleteItem(resourceId);
	    } else {
		createOrUpdateItem(resourceModel);
	    }
	}

	// Delete items no more on the server
	List<Item> locallyAddedItems = new ArrayList<Item>();
	for (Item item : itemsDbAdater.getAllItems()) {
	    if (!receivedItems.contains(item)) {
		if (item.isCreated()) {
		    locallyAddedItems.add(item);
		} else {
		    itemsDbAdater.deleteItem(item.getId());
		}
	    }
	}

	boolean hideProgressBar = true;
	Requester.initDownloads();
	// Start downloads
	for (Item item : receivedItems) {
	    if (item.needsToBeDownloaded()) {
		ItemsListFragment.instance.initProgressBar(item.getLength()
			.intValue());
		Requester.downloadItem(item);
		hideProgressBar = false;
	    }
	}

	// Start uploads
	for (Item item : locallyAddedItems) {
	    Requester.postItem(item);
	    hideProgressBar = false;
	}

	if (hideProgressBar) {
	    Utils.logInfo("Nothing to sync hideProgressBar");
	    ItemsListFragment.instance.hideProgressBar();
	}
    }

    private void createOrUpdateItem(JSONObject resourceModel)
	    throws JSONException {
	Item item = itemsDbAdater.fetchItem(resourceModel);
	if (item == null) {
	    item = itemsDbAdater.createItem(resourceModel);
	} else {
	    item = itemsDbAdater.updateItem(item, resourceModel);
	}
	receivedItems.add(item);
    }

    protected void syncFollowers(JSONArray followerModels) throws JSONException {
	followersDbAdater = AndroidTravelBookActivity.instance
		.getFollowersDbAdapter();
	receivedFollowers = new ArrayList<Follower>();

	// Try to delete followers asked to be deleted
	List<Long> deletedFollowers = deleteFollowers();

	// Create or update followers from the server
	for (int i = 0; i < followerModels.length(); ++i) {
	    JSONObject followerModel = (JSONObject) followerModels.get(i);
	    Long followerId = followerModel.getLong("id");
	    if (!deletedFollowers.contains(followerId)) {
		createOrUpdateFollower(followerModel);
	    }
	}

	List<Follower> locallyAddedFollowers = new ArrayList<Follower>();
	for (Follower follower : followersDbAdater.getAllFollowers()) {
	    if (!receivedFollowers.contains(follower)) {
		if (follower.isLocallyAdded()) {
		    locallyAddedFollowers.add(follower);
		} else {
		    followersDbAdater.deleteFollower(follower.getId());
		}
	    }
	}
	if (!locallyAddedFollowers.isEmpty()) {
	    Requester.postFollowers(locallyAddedFollowers);
	}
    }

    private void createOrUpdateFollower(JSONObject followerModel)
	    throws JSONException {
	Follower follower = followersDbAdater.fetchFollower(followerModel);
	if (follower == null) {
	    follower = followersDbAdater.createFollower(followerModel);
	} else {
	    follower = followersDbAdater
		    .updateFollower(follower, followerModel);
	}
	receivedFollowers.add(follower);
    }

    private List<Long> deleteFollowers() {
	List<Long> deletedFollowers = new ArrayList<Long>();
	Cursor cursor = followersDbAdater.fetchFollowersToBeDeleted();
	try {
	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
		final Long followerId = cursor.getLong(cursor
			.getColumnIndex(FollowersDbAdapter.KEY_ID));
		deletedFollowers.add(followerId);
		Long followerUserId = cursor.getLong(cursor
			.getColumnIndex(FollowersDbAdapter.KEY_USER_ID));
		Requester.removeFollower(followerId, followerUserId);
		cursor.moveToNext();
	    }
	} finally {
	    if (cursor != null) {
		cursor.close();
	    }
	}
	return deletedFollowers;
    }

    private List<Long> deleteItems() {
	List<Long> deletedItems = new ArrayList<Long>();
	Cursor cursor = itemsDbAdater.fetchItemsToBeDeleted();
	try {
	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
		final Long itemId = cursor.getLong(cursor
			.getColumnIndex(ItemsDbAdapter.KEY_ID));
		deletedItems.add(itemId);
		cursor.moveToNext();
	    }
	} finally {
	    if (cursor != null) {
		cursor.close();
	    }
	}
	return deletedItems;
    }

}
