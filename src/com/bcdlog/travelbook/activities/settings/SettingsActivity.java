package com.bcdlog.travelbook.activities.settings;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;

import com.bcdlog.travelbook.R;
import com.bcdlog.travelbook.TBPreferences;
import com.bcdlog.travelbook.network.Requester;

public class SettingsActivity extends Activity {

    protected static final String PRIVATE = "PRIVATE";
    protected static final String PUBLIC = "PUBLIC";
    protected static final String MASS = "MASS";
    protected RadioButton radioButtonPrivate;
    protected RadioButton radioButtonShared;
    protected RadioButton radioButtonPublished;
    protected Button archiveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.settings);

	getActionBar().setHomeButtonEnabled(true);

	initRadioButtons();
	radioButtonPrivate
		.setOnCheckedChangeListener(new OnCheckedChangeListener() {

		    @Override
		    public void onCheckedChanged(CompoundButton buttonView,
			    boolean isChecked) {
			if (isChecked) {
			    Requester.changeReceptionAlbumKind(
				    SettingsActivity.this, PRIVATE);
			}
		    }
		});
	radioButtonShared
		.setOnCheckedChangeListener(new OnCheckedChangeListener() {

		    @Override
		    public void onCheckedChanged(CompoundButton buttonView,
			    boolean isChecked) {
			if (isChecked) {
			    Requester.changeReceptionAlbumKind(
				    SettingsActivity.this, PUBLIC);
			}
		    }
		});
	radioButtonPublished
		.setOnCheckedChangeListener(new OnCheckedChangeListener() {

		    @Override
		    public void onCheckedChanged(CompoundButton buttonView,
			    boolean isChecked) {
			if (isChecked) {
			    Requester.changeReceptionAlbumKind(
				    SettingsActivity.this, MASS);
			}
		    }
		});

	archiveButton = (Button) findViewById(R.id.archive);
	archiveButton.setOnClickListener(new View.OnClickListener() {

	    @Override
	    public void onClick(View v) {
		startActivity(new Intent(SettingsActivity.this,
			ArchiveActivity.class));
		finish();
	    }

	});

	Button linkButton = (Button) findViewById(R.id.site_button);
	linkButton.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		String lang = "en";
		if (Locale.getDefault().getLanguage().equals("fr")) {
		    lang = "fr";
		}
		startActivity(new Intent(Intent.ACTION_VIEW, Uri
			.parse("http://carnet-de-voyage.appspot.com/" + lang
				+ "/support_android.html")));
		finish();
	    }
	});

	if (!Requester.isOnline(true)) {
	    radioButtonPrivate.setEnabled(false);
	    radioButtonShared.setEnabled(false);
	    radioButtonPublished.setEnabled(false);
	    archiveButton.setEnabled(false);
	    linkButton.setEnabled(false);
	}
    }

    protected void initRadioButtons() {
	radioButtonPrivate = (RadioButton) findViewById(R.id.radioButtonPrivate);
	radioButtonShared = (RadioButton) findViewById(R.id.radioButtonShared);
	radioButtonPublished = (RadioButton) findViewById(R.id.radioButtonPublished);
	String kind = TBPreferences.getReceptionAlbumKind();
	if (kind.equals(PUBLIC)) {
	    radioButtonShared.setChecked(true);
	} else if (kind.equals(MASS)) {
	    radioButtonPublished.setChecked(true);
	} else {
	    radioButtonPrivate.setChecked(true);
	}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
	finish();
	return true;
    }
}
