package com.bcdlog.travelbook.activities;

import java.util.Date;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.bcdlog.travelbook.FileStorage;
import com.bcdlog.travelbook.R;
import com.bcdlog.travelbook.Utils;
import com.bcdlog.travelbook.activities.items.ItemsListFragment;
import com.bcdlog.travelbook.database.Item;
import com.bcdlog.travelbook.database.ItemsDbAdapter;

public class AudioCaptureActivity extends Activity {

    private ImageButton imageButton;
    private MediaRecorder mRecorder;
    private ItemsDbAdapter itemsDbAdapter;
    private Item creatingItem;
    private boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.audio_capture);

	getActionBar().setHomeButtonEnabled(true);

	itemsDbAdapter = AndroidTravelBookActivity.instance.getItemsDbAdapter();
	creatingItem = itemsDbAdapter.createItem("audio/3gpp", ".3gp",
		new Date());

	try {
	    mRecorder = new MediaRecorder();
	    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	    FileStorage.getInstance().getItemFileOutputStream(creatingItem,
		    false);
	    mRecorder.setOutputFile(creatingItem.getFile().getAbsolutePath());
	    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
	} catch (Throwable t) {
	    FileStorage.getInstance().deleteFiles(creatingItem);
	    Utils.logError(t);
	    finish();
	    return;
	}

	imageButton = (ImageButton) findViewById(R.id.imageButton);
	imageButton.setOnClickListener(new View.OnClickListener() {

	    @Override
	    public void onClick(View v) {
		if (recording) {
		    mRecorder.stop();
		    ItemsListFragment.instance.addMediaItem(creatingItem, null);
		    finish();
		} else {
		    imageButton.setImageResource(R.drawable.audio_stop);
		    try {
			mRecorder.prepare();
			mRecorder.start();
		    } catch (Throwable t) {
			Utils.logError(t);
		    }

		}
		recording = !recording;
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

    @Override
    protected void onStop() {
	if (mRecorder != null) {
	    mRecorder.release();
	    mRecorder = null;
	}
	super.onStop();
    }

}
