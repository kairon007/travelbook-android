package com.bcdlog.travelbook.network;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.bcdlog.travelbook.activities.TBListFragment;
import com.bcdlog.travelbook.activities.items.ItemsListFragment;
import com.bcdlog.travelbook.database.Item;

public class TBInputStream extends FileInputStream {

    private final TBListFragment listActivity;

    public TBInputStream(Item item) throws FileNotFoundException {
	super(item.getFile());
	this.listActivity = ItemsListFragment.instance;
	if (listActivity != null) {
	    listActivity.initProgressBar((int) item.getFile().length());
	}
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount)
	    throws IOException {
	if (listActivity != null) {
	    listActivity.increaseProgressBar(byteCount);
	}
	return super.read(buffer, byteOffset, byteCount);
    }

}
