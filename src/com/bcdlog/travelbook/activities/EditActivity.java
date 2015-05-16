package com.bcdlog.travelbook.activities;

import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.bcdlog.travelbook.R;
import com.bcdlog.travelbook.activities.items.ItemsListFragment;
import com.bcdlog.travelbook.database.Item;

public class EditActivity extends Activity {

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.edit);
	editText = (EditText) findViewById(R.id.edit);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.cancel, menu);
	inflater.inflate(R.menu.save, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
	switch (menuItem.getItemId()) {
	case android.R.id.home:
	    finish();
	    return true;
	case R.id.save:
	    Item item = AndroidTravelBookActivity.instance.getItemsDbAdapter()
		    .createItem("text/html", ".html", new Date());
	    AndroidTravelBookActivity.instance.saveDescription(item, editText
		    .getText().toString(), item.getDate());
	    finish();
	    ItemsListFragment.instance.updateDisplay();
	    return true;
	case R.id.cancel:
	    finish();
	    return true;
	default:
	    return super.onOptionsItemSelected(menuItem);
	}
    }
}
