package com.bcdlog.travelbook.activities.items;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import com.bcdlog.travelbook.R;
import com.bcdlog.travelbook.TBPreferences;
import com.bcdlog.travelbook.activities.TBListFragment;
import com.bcdlog.travelbook.activities.gallery.GalleryActivity;
import com.bcdlog.travelbook.database.Item;

public class ItemClickHandler implements OnClickListener, OnLongClickListener {

    private final int position;
    private final TBListFragment listFragment;
    private final Item item;

    public ItemClickHandler(TBListFragment listFragment, int position, Item item) {
	this.listFragment = listFragment;
	this.position = position;
	this.item = item;
    }

    @Override
    public void onClick(View v) {
	Bundle bundle = new Bundle();
	bundle.putLong("userId", TBPreferences.getUserId());
	bundle.putInt("position", position);
	bundle.putBoolean("fullscreen", true);
	Intent intent = new Intent(listFragment.getActivity(),
		GalleryActivity.class);
	intent.putExtras(bundle);
	listFragment.getActivity().startActivity(intent);
    }

    @Override
    public boolean onLongClick(View v) {
	AlertDialog.Builder builder = new AlertDialog.Builder(
		listFragment.getActivity());
	String message = listFragment.getActivity().getResources()
		.getString(R.string.delete_item, item.getName());
	builder.setMessage(message)
		.setCancelable(false)
		.setPositiveButton(R.string.yes,
			new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int id) {
				listFragment.delete(item);
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
