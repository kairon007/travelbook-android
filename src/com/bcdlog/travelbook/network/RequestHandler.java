package com.bcdlog.travelbook.network;

import java.io.InputStream;

public interface RequestHandler {

	String getUri();

	void onSuccess(InputStream inputStream);

	void onFailure();
}
