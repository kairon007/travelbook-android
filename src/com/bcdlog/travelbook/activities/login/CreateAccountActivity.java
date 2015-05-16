package com.bcdlog.travelbook.activities.login;

import java.io.InputStream;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.bcdlog.travelbook.R;
import com.bcdlog.travelbook.network.RequestHandler;
import com.bcdlog.travelbook.network.Requester;

public class CreateAccountActivity extends Activity {

    final private static int SUSSESS = 0;
    final private static int FAILURE = 1;

    private EditText emailField;
    private TextView status;
    private ProgressBar spinner;

    // Define the Handler that receives messages from external threads
    final private Handler handler = new Handler() {

	@Override
	public void handleMessage(Message msg) {
	    backFromWaiting();
	    switch (msg.what) {
	    case SUSSESS:
		status.setTextColor(Color.WHITE);
		status.setText(R.string.send_success);
		break;
	    case FAILURE:
		status.setTextColor(Color.RED);
		status.setText(R.string.send_failure);
		break;
	    }
	}

	private void backFromWaiting() {
	    status.setVisibility(View.VISIBLE);
	    spinner.setVisibility(View.INVISIBLE);
	}

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.ask_email);

	getActionBar().setHomeButtonEnabled(true);

	emailField = (EditText) findViewById(R.id.email);
	emailField.setOnEditorActionListener(new OnEditorActionListener() {

	    @Override
	    public boolean onEditorAction(TextView v, int actionId,
		    KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_GO) {
		    // Hide keyboard
		    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
			    .hideSoftInputFromWindow(
				    emailField.getWindowToken(), 0);

		    // Hide status
		    status.setVisibility(View.INVISIBLE);

		    // Show spinner
		    spinner.setVisibility(View.VISIBLE);
		    emailField.setEnabled(false);

		    // Get inputStream from the network
		    new Requester().get(new RequestHandler() {

			@Override
			public String getUri() {
			    return "/jsonCreateAccount?email="
				    + emailField.getText() + "&lang="
				    + Locale.getDefault().getLanguage();
			}

			@Override
			public void onSuccess(InputStream inputStream) {
			    Message message = Message.obtain();
			    message.what = SUSSESS;
			    handler.sendMessage(message);
			}

			@Override
			public void onFailure() {
			    Message message = Message.obtain();
			    message.what = FAILURE;
			    handler.sendMessage(message);
			}
		    });
		}
		return false;
	    }
	});
	status = (TextView) findViewById(R.id.status);
	spinner = (ProgressBar) findViewById(R.id.spinner);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
	switch (menuItem.getItemId()) {
	case android.R.id.home:
	    finish();
	    return true;
	}
	return false;
    }
}
