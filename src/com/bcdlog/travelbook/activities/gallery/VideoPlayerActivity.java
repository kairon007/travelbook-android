package com.bcdlog.travelbook.activities.gallery;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.VideoView;

import com.bcdlog.travelbook.R;
import com.bcdlog.travelbook.Utils;

public class VideoPlayerActivity extends Activity implements
	OnCompletionListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.video_activity);

	getActionBar().setHomeButtonEnabled(true);

	try {
	    VideoView videoView = (VideoView) findViewById(R.id.video_view);
	    Bundle bundle = getIntent().getExtras();
	    String path = bundle.getString("path");
	    videoView.setVideoPath(path);
	    videoView.setOnCompletionListener(this);
	    videoView.setOnErrorListener(new OnErrorListener() {

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
		    finish();
		    return false;
		}
	    });
	    // videoView.setMediaController(new MediaController(this));
	    videoView.start();
	} catch (Throwable t) {
	    Utils.logError(t);
	    Utils.toast(this, getResources().getString(R.string.video_failure));
	    finish();
	}
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
	finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
	finish();
	return true;
    }
}
