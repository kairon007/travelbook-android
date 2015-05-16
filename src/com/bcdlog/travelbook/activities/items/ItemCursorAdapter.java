package com.bcdlog.travelbook.activities.items;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bcdlog.travelbook.R;
import com.bcdlog.travelbook.Utils;
import com.bcdlog.travelbook.activities.AndroidTravelBookActivity;
import com.bcdlog.travelbook.database.Item;
import com.bcdlog.travelbook.database.ItemsDbAdapter;

public class ItemCursorAdapter extends CursorAdapter {

    private final ItemsListFragment itemsListFragment;
    private final ItemsDbAdapter itemsDbAdapter;

    public ItemCursorAdapter(ItemsListFragment itemsListFragment, Cursor cursor) {
	super(itemsListFragment.getActivity(), cursor, false);
	this.itemsListFragment = itemsListFragment;
	this.itemsDbAdapter = AndroidTravelBookActivity.instance
		.getItemsDbAdapter();
    }

    @Override
    public View newView(Context context, final Cursor cursor, ViewGroup parent) {
	View view = itemsListFragment.getActivity().getLayoutInflater()
		.inflate(R.layout.items_list_item, null);
	return view;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
	Item item = itemsDbAdapter.extractFrom(cursor);
	ItemClickHandler clickHandler = new ItemClickHandler(itemsListFragment,
		cursor.getPosition(), item);

	TextView contentView = (TextView) view.findViewById(R.id.content);
	String content = cursor.getString(cursor
		.getColumnIndex(ItemsDbAdapter.KEY_CONTENT));
	contentView.setText(content);
	contentView.setOnClickListener(clickHandler);
	contentView.setOnLongClickListener(clickHandler);

	TextView dateView = (TextView) view.findViewById(R.id.date);
	dateView.setText(Utils.prettyDate(cursor.getString(cursor
		.getColumnIndex(ItemsDbAdapter.KEY_DATE))));
	dateView.setOnClickListener(clickHandler);
	dateView.setOnLongClickListener(clickHandler);

	ImageView icon = (ImageView) view.findViewById(R.id.icon);
	if (item.isText()) {
	    icon.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
	    contentView.setWidth(AndroidTravelBookActivity.instance
		    .getWindowManager().getDefaultDisplay().getWidth());
	} else {
	    Utils.createIcon(item, icon);
	    icon.setLayoutParams(new LinearLayout.LayoutParams(context
		    .getResources().getDimensionPixelSize(
			    R.dimen.thumbnail_width), context.getResources()
		    .getDimensionPixelSize(R.dimen.thumbnail_width)));
	    icon.setOnClickListener(clickHandler);
	    icon.setOnLongClickListener(clickHandler);
	    contentView.setWidth(AndroidTravelBookActivity.instance
		    .getWindowManager().getDefaultDisplay().getWidth()
		    - icon.getWidth());
	}

    }
}
