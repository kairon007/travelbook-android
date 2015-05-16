/***
  Copyright (c) 2008-2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  From _The Busy Coder's Guide to Advanced Android Development_
    http://commonsware.com/AdvAndroid
 */

package com.bcdlog.travelbook.activities.gallery;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bcdlog.travelbook.R;
import com.bcdlog.travelbook.Utils;

public class AudioPlayerActivity extends Activity implements
	MediaPlayer.OnCompletionListener {

    private ImageButton playPause;
    private MediaPlayer mp;
    private Uri uri;
    private ProgressBar progressBar;
    private Timer timer;

    @Override
    public void onCreate(Bundle icicle) {
	super.onCreate(icicle);
	Bundle bundle = getIntent().getExtras();
	setContentView(R.layout.audio_view);

	getActionBar().setHomeButtonEnabled(true);

	playPause = (ImageButton) findViewById(R.id.play_pause);

	playPause.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View view) {
		action();
	    }
	});

	progressBar = (ProgressBar) findViewById(R.id.progressBar);

	TextView description = (TextView) findViewById(R.id.description);
	description.setText(bundle.getString("description"));

	String path = bundle.getString("path");
	TextView name = (TextView) findViewById(R.id.name);
	name.setText(path.substring(path.lastIndexOf("/") + 1));
	try {
	    uri = Uri.fromFile(new File(path));
	    mp = MediaPlayer.create(this, uri);
	    int seconds = mp.getDuration() / 1000;
	    TextView duration = (TextView) findViewById(R.id.duration);
	    duration.setText((seconds / 60) + ":" + (seconds % 60));
	    mp.setOnCompletionListener(this);
	    progressBar.setMax(mp.getDuration());
	    action();
	} catch (Throwable t) {
	    Utils.logError(t);
	    Utils.toast(this, getResources().getString(R.string.audio_failure));
	    finish();
	}
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
	if (timer != null) {
	    timer.cancel();
	}
	if (mp != null) {
	    try {
		mp.stop();
	    } catch (Throwable t) {
		Utils.logError(t);
	    }

	    try {
		mp.release();
	    } catch (Throwable t) {
		Utils.logError(t);
	    }
	}
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
	finish();
    }

    private void action() {
	if (mp.isPlaying()) {
	    mp.pause();
	    playPause.setImageResource(R.drawable.audio_play);
	} else {
	    timer = new Timer();
	    mp.start();
	    timer.scheduleAtFixedRate(new TimerTask() {

		@Override
		public void run() {
		    runOnUiThread(new Runnable() {

			@Override
			public void run() {
			    try {
				progressBar.setProgress(mp.getCurrentPosition());
			    } catch (Throwable t) {
				cancel();
			    }
			}
		    });
		}
	    }, 0, 100);
	    playPause.setImageResource(R.drawable.audio_pause);
	}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
	finish();
	return true;
    }

    private void goBlooey(Throwable t) {
	AlertDialog.Builder builder = new AlertDialog.Builder(this);

	builder.setTitle("Exception!").setMessage(t.toString())
		.setPositiveButton("OK", null).show();
    }

}