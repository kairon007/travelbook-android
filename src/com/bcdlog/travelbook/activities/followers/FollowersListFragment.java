package com.bcdlog.travelbook.activities.followers;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bcdlog.travelbook.R;
import com.bcdlog.travelbook.Utils;
import com.bcdlog.travelbook.activities.AndroidTravelBookActivity;
import com.bcdlog.travelbook.activities.TBListFragment;
import com.bcdlog.travelbook.database.Bean;
import com.bcdlog.travelbook.database.Follower;
import com.bcdlog.travelbook.database.FollowersDbAdapter;
import com.bcdlog.travelbook.network.Requester;

public class FollowersListFragment extends TBListFragment {

    public static FollowersListFragment instance;

    private FollowersDbAdapter followersDbAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
	View view = inflater.inflate(R.layout.followers_list, container, false);
	instance = this;
	// init(view);
	followersDbAdapter = AndroidTravelBookActivity.instance
		.getFollowersDbAdapter();
	fillData();

	return view;
    }

    @Override
    protected int fillData() {
	Cursor cursor = followersDbAdapter.fetchFollowers();
	getActivity().startManagingCursor(cursor);
	mAdapter = new FollowerCursorAdapter(this, cursor);
	setListAdapter(mAdapter);
	return cursor.getCount();
    }

    public void addContact(Uri uri) {
	Cursor cursor = null;
	try {

	    cursor = getActivity().getContentResolver().query(uri, null, null,
		    null, null);
	    if (cursor != null) {
		if (cursor.moveToFirst()) {
		    String id = cursor.getString(cursor
			    .getColumnIndex(ContactsContract.Contacts._ID));
		    final String name = cursor
			    .getString(cursor
				    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		    Cursor emailsCursor = getActivity()
			    .getContentResolver()
			    .query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
				    null,
				    ContactsContract.CommonDataKinds.Email.CONTACT_ID
					    + " = ?", new String[] { id }, null);
		    if (emailsCursor != null) {
			addContact(emailsCursor, name);
		    }
		}
	    }
	} finally {
	    if (cursor != null) {
		cursor.close();
	    }
	}
    }

    private void addContact(Cursor emailsCursor, final String name) {
	try {
	    switch (emailsCursor.getCount()) {
	    case 0:
		Utils.toast(getActivity(),
			getResources().getString(R.string.email_missing, name));
		break;
	    case 1:
		emailsCursor.moveToFirst();
		String theEmail = emailsCursor
			.getString(emailsCursor
				.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
		saveFollower(name, theEmail);
		break;
	    default:
		final Dialog dlg = new Dialog(getActivity());
		dlg.setTitle(R.string.select_email);
		LayoutInflater li = (LayoutInflater) getActivity()
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View emailsListLayout = li.inflate(R.layout.emails_list, null,
			false);
		ListView emailsView = (ListView) emailsListLayout
			.findViewById(R.id.emails);
		emailsView.setOnItemClickListener(new OnItemClickListener() {

		    @Override
		    public void onItemClick(AdapterView<?> parent, View view,
			    int position, long id) {
			dlg.dismiss();
			TextView emailView = (TextView) view
				.findViewById(R.id.email);
			saveFollower(name, emailView.getText().toString());
		    }
		});
		ListAdapter listAdapter = new CursorAdapter(getActivity(),
			emailsCursor, false) {

		    @Override
		    public View newView(Context context, Cursor cursor,
			    ViewGroup parent) {
			View view = getActivity().getLayoutInflater().inflate(
				R.layout.emails_item, null);
			bindView(view, context, cursor);
			return view;
		    }

		    @Override
		    public void bindView(View view, Context context,
			    Cursor cursor) {
			TextView emailView = (TextView) view
				.findViewById(R.id.email);
			emailView
				.setText(cursor.getString(cursor
					.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));

			TextView emailTypeView = (TextView) view
				.findViewById(R.id.email_type);
			int type = cursor
				.getInt(cursor
					.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
			String customLabel = cursor
				.getString(cursor
					.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL));
			CharSequence emailType = ContactsContract.CommonDataKinds.Email
				.getTypeLabel(context.getResources(), type,
					customLabel);
			emailTypeView.setText(emailType);
		    }
		};
		emailsView.setAdapter(listAdapter);
		dlg.setContentView(emailsListLayout);
		dlg.show();
		break;
	    }
	} finally {
	    if (emailsCursor != null) {
		emailsCursor.close();
	    }
	}
    }

    public void saveFollower(String contact, String email) {
	// Check if email already exists into followers
	Follower follower = followersDbAdapter.fetchFollower(email, contact);
	if (follower == null) {
	    follower = new Follower();
	    follower.setEmail(email);
	    follower.setContact(contact);
	    follower.setStatus(FollowersDbAdapter.Status.LOCALLY_ADDED
		    .toString());
	    followersDbAdapter.createFollower(follower);
	    // Post follower
	    List<Follower> locallyAddedFollowers = new ArrayList<Follower>();
	    locallyAddedFollowers.add(follower);
	    Requester.postFollowers(locallyAddedFollowers);
	} else if (follower.isDeleted()) {
	    // Cancel delete
	    follower.setStatus(null);
	    followersDbAdapter.updateFollower(follower);
	}
    }

    @Override
    public void delete(Bean bean) {
	Follower follower = (Follower) bean;
	if (follower.isLocallyAdded()) {
	    followersDbAdapter.deleteFollower(follower.getId());
	} else {
	    follower.setStatus(FollowersDbAdapter.Status.DELETED.toString());
	    followersDbAdapter.updateFollower(follower);
	    Requester.removeFollower(follower.getId(), follower.getUserId());
	}
	fillData();
    }
}
