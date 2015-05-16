package com.bcdlog.travelbook.activities.followers;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.bcdlog.travelbook.R;
import com.bcdlog.travelbook.Utils;
import com.bcdlog.travelbook.activities.AndroidTravelBookActivity;
import com.bcdlog.travelbook.database.Follower;
import com.bcdlog.travelbook.database.FollowersDbAdapter;

public class FollowerCursorAdapter extends CursorAdapter {

    private final FollowersListFragment followersListFragment;
    private final FollowersDbAdapter followersDbAdapter;

    public FollowerCursorAdapter(FollowersListFragment followersListFragment,
	    Cursor cursor) {
	super(followersListFragment.getActivity(), cursor, false);
	this.followersListFragment = followersListFragment;
	this.followersDbAdapter = AndroidTravelBookActivity.instance
		.getFollowersDbAdapter();
    }

    @Override
    public View newView(Context context, final Cursor cursor, ViewGroup parent) {
	View view = followersListFragment.getActivity().getLayoutInflater()
		.inflate(R.layout.followers_list_item, null);
	return view;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
	Follower follower = followersDbAdapter.extractFrom(cursor);
	FollowerClickHandler clickHandler = new FollowerClickHandler(
		followersListFragment, follower);

	TextView textView = (TextView) view.findViewById(R.id.name);
	String name = cursor.getString(cursor
		.getColumnIndex(FollowersDbAdapter.KEY_CONTACT));
	if (name == null) {
	    name = cursor.getString(cursor
		    .getColumnIndex(FollowersDbAdapter.KEY_NICKNAME));
	}
	if (name == null) {
	    name = cursor.getString(cursor
		    .getColumnIndex(FollowersDbAdapter.KEY_EMAIL));
	}
	String nameAndItems = name;
	if (follower.getUserId() != null) {
	    Cursor itemsCursor = AndroidTravelBookActivity.instance
		    .getItemsDbAdapter().fetchUserItems(follower.getUserId());
	    try {
		int items = itemsCursor.getCount();
		if (items > 0) {
		    nameAndItems = nameAndItems + " (" + items + ")";
		}
	    } catch (Throwable t) {
		Utils.logError(t);
	    } finally {
		if (itemsCursor != null) {
		    itemsCursor.close();
		}
	    }
	}
	textView.setText(nameAndItems);
	textView.setOnClickListener(clickHandler);
	textView.setOnLongClickListener(clickHandler);
    }
}
