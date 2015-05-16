package com.bcdlog.travelbook.activities;

import android.app.ListFragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;

import com.bcdlog.travelbook.R;
import com.bcdlog.travelbook.Utils;
import com.bcdlog.travelbook.database.Bean;

public abstract class TBListFragment extends ListFragment {

    private static final int FILL_DATA = 0;
    private static final int SHOW_PROGRESS_BAR = 1;
    private static final int INIT_PROGRESS_BAR = 2;
    private static final int INCREASE_PROGRESS_BAR = 3;
    private static final int HIDE_PROGRESS_BAR = 4;
    private static final int DOWNLOAD_FAILURE = 5;
    private static final int SET_PROGRESS_BAR = 6;

    protected ListAdapter mAdapter;
    private ProgressBar progressBar;
    private LinearLayout progressBarWidget;

    abstract protected int fillData();

    public abstract void delete(Bean bean);

    protected void init(View view) {
	progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
	progressBarWidget = (LinearLayout) view
		.findViewById(R.id.progressBarWidget);
    }

    // Define the Handler that receives messages from external threads
    final private Handler handler = new Handler() {

	@Override
	public void handleMessage(Message msg) {
	    switch (msg.what) {
	    case FILL_DATA:
		fillData();
		break;
	    case SHOW_PROGRESS_BAR:
		getProgressBar().setMax(0);
		getProgressBar().setProgress(0);
		progressBarWidget.setVisibility(View.VISIBLE);
		break;
	    case INIT_PROGRESS_BAR:
		int total = msg.getData().getInt("total");
		Utils.logInfo("INIT_PROGRESS_BAR with " + total
			+ " new total = " + (getProgressBar().getMax() + total));
		getProgressBar().setMax(getProgressBar().getMax() + total);
		progressBarWidget.setVisibility(View.VISIBLE);
		break;
	    case INCREASE_PROGRESS_BAR:
		int progress = msg.getData().getInt("progress");
		if (progress > 0
			&& getProgressBar().getProgress() + progress < getProgressBar()
				.getMax()) {
		    getProgressBar().incrementProgressBy(progress);
		} else {
		    finishProgress();
		}
		break;
	    case HIDE_PROGRESS_BAR:
		finishProgress();
		break;
	    case DOWNLOAD_FAILURE:
		Utils.toast(
			TBListFragment.this.getActivity(),
			TBListFragment.this.getResources().getString(
				R.string.download_failure,
				msg.getData().getString("text")));
		break;
	    case SET_PROGRESS_BAR:
		int p = msg.getData().getInt("progress");
		if (p < getProgressBar().getMax()) {
		    getProgressBar().setProgress(p);
		} else {
		    finishProgress();
		}
		break;
	    }
	}

	private void finishProgress() {
	    progressBarWidget.setVisibility(View.INVISIBLE);
	    getProgressBar().setMax(0);
	    getProgressBar().setProgress(0);
	}

    };

    public void updateDisplay() {
	Message message = Message.obtain();
	message.what = FILL_DATA;
	handler.sendMessage(message);
    }

    public void showProgressBar() {
	Message message = Message.obtain();
	message.what = SHOW_PROGRESS_BAR;
	handler.sendMessage(message);
    }

    public void initProgressBar(int total) {
	Message message = Message.obtain();
	message.what = INIT_PROGRESS_BAR;
	Bundle data = new Bundle();
	data.putInt("total", total);
	message.setData(data);
	handler.sendMessage(message);
    }

    public void increaseProgressBar(int progress) {
	Message message = Message.obtain();
	message.what = INCREASE_PROGRESS_BAR;
	Bundle data = new Bundle();
	data.putInt("progress", progress);
	message.setData(data);
	handler.sendMessage(message);
    }

    /**
     * @return the progressBar
     */
    public ProgressBar getProgressBar() {
	return progressBar;
    }

    public void hideProgressBar() {
	Message message = Message.obtain();
	message.what = HIDE_PROGRESS_BAR;
	handler.sendMessage(message);
    }

    public void downloadFailure(String text) {
	Message message = Message.obtain();
	message.what = DOWNLOAD_FAILURE;
	Bundle data = new Bundle();
	data.putString("text", text);
	message.setData(data);
	handler.sendMessage(message);
    }

    public void setProgressBar(int progress) {
	Message message = Message.obtain();
	message.what = SET_PROGRESS_BAR;
	Bundle data = new Bundle();
	data.putInt("progress", progress);
	message.setData(data);
	handler.sendMessage(message);
    }

}
