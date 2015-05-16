package com.bcdlog.travelbook.activities.followers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import com.bcdlog.travelbook.R;
import com.bcdlog.travelbook.activities.TBListFragment;
import com.bcdlog.travelbook.activities.gallery.GalleryActivity;
import com.bcdlog.travelbook.database.Follower;

public class FollowerClickHandler implements OnClickListener,
	OnLongClickListener {

    private final TBListFragment listFragment;
    private final Follower follower;

    public FollowerClickHandler(TBListFragment listFragment, Follower follower) {
	this.listFragment = listFragment;
	this.follower = follower;
    }

    @Override
    public void onClick(View v) {
	if (follower.getItems() != null && follower.getItems().longValue() > 0) {
	    Bundle bundle = new Bundle();
	    bundle.putLong("userId", follower.getUserId());
	    bundle.putBoolean("fullscreen", true);
	    Intent intent = new Intent(listFragment.getActivity(),
		    GalleryActivity.class);
	    intent.putExtras(bundle);
	    listFragment.getActivity().startActivity(intent);
	}
    }

    @Override
    public boolean onLongClick(View v) {
	AlertDialog.Builder builder = new AlertDialog.Builder(
		listFragment.getActivity());
	String message = listFragment.getActivity().getResources()
		.getString(R.string.delete_contact, follower.getName());
	builder.setMessage(message)
		.setCancelable(false)
		.setPositiveButton(R.string.yes,
			new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int id) {
				listFragment.delete(follower);
				dialog.dismiss();
			    }
			})
		.setNegativeButton(R.string.no,
			new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			    }
			});
	AlertDialog alert = builder.create();
	alert.show();
	return true;
    }

}
