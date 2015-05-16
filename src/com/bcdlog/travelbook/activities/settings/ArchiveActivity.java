package com.bcdlog.travelbook.activities.settings;

import java.util.Calendar;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bcdlog.travelbook.R;
import com.bcdlog.travelbook.TBPreferences;
import com.bcdlog.travelbook.activities.EditTextWithDone;
import com.bcdlog.travelbook.activities.EditTextWithNext;
import com.bcdlog.travelbook.network.Requester;

public class ArchiveActivity extends SettingsActivity {

    private EditText name;
    private EditText year;
    private EditText description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.archive);

	getActionBar().setHomeButtonEnabled(true);

	name = (EditTextWithNext) findViewById(R.id.name);
	String albumName = TBPreferences.getReceptionAlbumName();
	name.setText(albumName);
	name.setSelection(albumName.length());

	year = (EditTextWithNext) findViewById(R.id.year);
	int currentYear = Calendar.getInstance().get(Calendar.YEAR);
	String y = String.valueOf(currentYear);
	year.setText(y);
	year.setSelection(y.length());

	initRadioButtons();

	description = (EditTextWithDone) findViewById(R.id.description);
	String desc = TBPreferences.getReceptionAlbumDescription();
	description.setText(desc);
	description.setSelection(desc.length());

	archiveButton = (Button) findViewById(R.id.archive);
	archiveButton.setOnClickListener(new View.OnClickListener() {

	    @Override
	    public void onClick(View v) {
		Requester.archiveReceptionAlbum(name.getText().toString(), year
			.getText().toString(), kindFromRadioButtons(),
			description.getText().toString());
		finish();
	    }

	    private String kindFromRadioButtons() {
		if (radioButtonPublished.isChecked()) {
		    return MASS;
		} else if (radioButtonPrivate.isChecked()) {
		    return PRIVATE;
		} else {
		    return PUBLIC;
		}
	    }
	});
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
	switch (menuItem.getItemId()) {
	case android.R.id.home:
	    finish();
	    return true;
	default:
	    return super.onOptionsItemSelected(menuItem);
	}
    }
}
