package com.bcdlog.travelbook;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;

import com.bcdlog.travelbook.network.Requester;

public class TBApplication extends Application {

    @Override
    public void onCreate() {
	super.onCreate();
	Requester
		.setConnectivityManager((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

	TBPreferences.init(this);

    }

}
