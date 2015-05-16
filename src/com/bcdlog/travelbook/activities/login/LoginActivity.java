package com.bcdlog.travelbook.activities.login;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.bcdlog.travelbook.R;
import com.bcdlog.travelbook.TBPreferences;
import com.bcdlog.travelbook.Utils;
import com.bcdlog.travelbook.activities.AndroidTravelBookActivity;
import com.bcdlog.travelbook.network.RequestHandler;
import com.bcdlog.travelbook.network.Requester;

/**
 * @author bcdlog
 * 
 */
public class LoginActivity extends Activity {

    final private static int LOGIN_SUSSESS = 0;
    final private static int LOGIN_FAILURE = 1;

    // Actions
    public static final String REFRESH = "REFRESH";
    public static final String NOTHING = "NOTHING";
    public static final String LOGIN = "LOGIN";

    private EditText loginField;
    private EditText passwordField;
    private TextView status;
    private ProgressBar spinner;

    // Define the Handler that receives messages from external threads
    final private Handler handler = new Handler() {

	@Override
	public void handleMessage(Message msg) {
	    switch (msg.what) {
	    case LOGIN_SUSSESS:
		finish();
		break;
	    case LOGIN_FAILURE:
		backFromWaiting();
		status.setText(R.string.login_error);
		break;
	    }
	}

	private void backFromWaiting() {
	    spinner.setVisibility(View.INVISIBLE);
	    // Enable buttons
	    connectButton.setEnabled(true);
	    createAccountButton.setVisibility(View.VISIBLE);
	    forgotPasswordButton.setVisibility(View.VISIBLE);

	    loginField.setEnabled(true);
	    passwordField.setEnabled(true);

	}

    };

    private Button createAccountButton;
    private Button forgotPasswordButton;
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	String token = TBPreferences.getToken();
	if (token == null) {
	    Uri data = getIntent().getData();
	    if (data != null) {
		token = data.getQueryParameter("token");
		if (token != null) {
		    TBPreferences.setToken(token);
		    startMainActivity(REFRESH);
		}
	    }
	    if (token == null) {
		showInterface();
	    }

	} else {
	    startMainActivity(NOTHING);
	}

    }

    private void startMainActivity(String action) {
	Intent intent = new Intent(this, AndroidTravelBookActivity.class);
	intent.setAction(action);
	startActivity(intent);
	finish();
    }

    private void showInterface() {
	setContentView(R.layout.login);

	getActionBar().hide();

	loginField = (EditText) findViewById(R.id.login);
	passwordField = (EditText) findViewById(R.id.password);
	passwordField.setOnEditorActionListener(new OnEditorActionListener() {

	    @Override
	    public boolean onEditorAction(TextView v, int actionId,
		    KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_GO) {
		    connect();
		}
		return false;
	    }
	});
	status = (TextView) findViewById(R.id.status);
	spinner = (ProgressBar) findViewById(R.id.spinner);
	createAccountButton = (Button) findViewById(R.id.createAccount);
	forgotPasswordButton = (Button) findViewById(R.id.forgotPassword);
	connectButton = (Button) findViewById(R.id.connect);

	connectButton.setOnClickListener(new View.OnClickListener() {

	    @Override
	    public void onClick(View v) {
		connect();

	    }

	});

	View.OnClickListener clickListener = new View.OnClickListener() {

	    @Override
	    public void onClick(View v) {
		Intent intent = new Intent(LoginActivity.this,
			CreateAccountActivity.class);
		startActivity(intent);

	    }
	};

	createAccountButton.setOnClickListener(clickListener);
	forgotPasswordButton.setOnClickListener(clickListener);

    }

    protected void connect() {
	status.setText("");

	// Hide keyboard
	((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
		.hideSoftInputFromWindow(loginField.getWindowToken(), 0);

	waiting();

	// Get inputStream from the network
	new Requester().get(new RequestHandler() {

	    @Override
	    public String getUri() {
		try {
		    return "/jsonAuthentication?login="
			    + URLEncoder.encode(
				    loginField.getText().toString(), "UTF-8")
			    + "&password="
			    + URLEncoder.encode(passwordField.getText()
				    .toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
		    Utils.logError(e);
		    onFailure();
		}
		return null;
	    }

	    @Override
	    public void onSuccess(InputStream inputStream) {
		try {
		    AndroidTravelBookActivity.jsonObjectFromLogin = new JSONObject(
			    Utils.inputstreamToString(inputStream));
		    Message message = Message.obtain();
		    message.what = LOGIN_SUSSESS;
		    handler.sendMessage(message);
		    startMainActivity(LOGIN);
		} catch (Throwable t) {
		    Utils.logError(t);
		    onFailure();
		}
	    }

	    @Override
	    public void onFailure() {
		Message message = Message.obtain();
		message.what = LOGIN_FAILURE;
		handler.sendMessage(message);
	    }
	});
    }

    private void waiting() {
	// Show spinner
	spinner.setVisibility(View.VISIBLE);

	// Disable buttons
	connectButton.setEnabled(false);
	createAccountButton.setVisibility(View.INVISIBLE);
	forgotPasswordButton.setVisibility(View.INVISIBLE);

	loginField.setEnabled(false);
	passwordField.setEnabled(false);

    }

}
