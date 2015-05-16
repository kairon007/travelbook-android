package com.bcdlog.travelbook.network;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.bcdlog.travelbook.FileStorage;
import com.bcdlog.travelbook.R;
import com.bcdlog.travelbook.TBPreferences;
import com.bcdlog.travelbook.Utils;
import com.bcdlog.travelbook.activities.AndroidTravelBookActivity;
import com.bcdlog.travelbook.activities.TBListFragment;
import com.bcdlog.travelbook.activities.items.ItemsListFragment;
import com.bcdlog.travelbook.activities.login.LoginActivity;
import com.bcdlog.travelbook.database.Follower;
import com.bcdlog.travelbook.database.Item;
import com.bcdlog.travelbook.database.ItemsDbAdapter;

public class Requester {

    private static final String SERVER_BASE_URL = "https://carnet-de-voyage.appspot.com";

    private static ConnectivityManager connectivityManager;

    private static DownloadManager downloadManager;

    private static Map<Long, Item> enqueuedDownloads;

    public static boolean isSynchronizing = false;

    private static int runningUploads;

    private static int runningDownloads;

    private static int alreadyDownloaded;

    private static Timer timer;

    private static boolean timerIsRunning = false;

    static {
	try {
	    _FakeX509TrustManager.allowAllSSL();
	} catch (Throwable t) {
	    Utils.logError(t.getMessage());
	}
    }

    public static boolean isOnline(boolean toast) {
	NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
	boolean online = false;
	if (networkInfo != null) {
	    online = connectivityManager.getActiveNetworkInfo().isConnected();
	}
	if (toast && !online) {
	    Context context = AndroidTravelBookActivity.instance;
	    Utils.toast(
		    context,
		    context.getResources().getString(
			    R.string.network_unreachable));
	}
	return online;
    }

    /**
     * Send a request over the network
     * 
     * @param requestHandler
     */
    public void get(RequestHandler requestHandler) {
	if (isOnline(true)) {
	    HttpGet httpGet = new HttpGet(SERVER_BASE_URL
		    + requestHandler.getUri());
	    performHttpRequest(httpGet, requestHandler);
	} else {
	    requestHandler.onFailure();
	}
    }

    /**
     * Send a request over the network
     * 
     * @param requestHandler
     * @throws UnsupportedEncodingException
     */
    private void postJSON(RequestHandler requestHandler, final String json)
	    throws UnsupportedEncodingException {
	HttpPost httpPost = new HttpPost(SERVER_BASE_URL
		+ requestHandler.getUri());
	httpPost.setEntity(new StringEntity(json, "UTF-8"));

	httpPost.addHeader(new Header() {

	    @Override
	    public String getValue() {
		return "application/json;charset=UTF-8";
	    }

	    @Override
	    public String getName() {
		return "Content-Type";
	    }

	    @Override
	    public HeaderElement[] getElements() throws ParseException {
		return null;
	    }
	});

	performHttpRequest(httpPost, requestHandler);

    }

    private void performHttpRequest(final HttpUriRequest httpUriRequest,
	    final RequestHandler requestHandler) {
	new Thread() {

	    @Override
	    public void run() {
		try {
		    _FakeX509TrustManager.allowAllSSL();

		    HttpParams httpParameters = new BasicHttpParams();
		    // Set the timeout in milliseconds until a connection is
		    // established.
		    int timeoutConnection = 300000;
		    HttpConnectionParams.setConnectionTimeout(httpParameters,
			    timeoutConnection);
		    // Set the default socket timeout (SO_TIMEOUT)
		    // in milliseconds which is the timeout for waiting for
		    // data.
		    int timeoutSocket = 300000;
		    HttpConnectionParams.setSoTimeout(httpParameters,
			    timeoutSocket);

		    HttpClient httpClient = new DefaultHttpClient(
			    httpParameters);
		    HttpResponse response = httpClient.execute(httpUriRequest);
		    StatusLine statusLine = response.getStatusLine();
		    int statusCode = statusLine.getStatusCode();
		    if (statusCode == 200) {
			HttpEntity entity = response.getEntity();
			requestHandler.onSuccess(entity.getContent());
		    } else {
			Utils.logError("Request " + requestHandler.getUri()
				+ " failed statusCode=" + statusCode);
			checkIfUserHasBeenDeleted(response);
			requestHandler.onFailure();
		    }
		} catch (Throwable t) {
		    Utils.logError("Request " + requestHandler.getUri()
			    + " error : " + t.getMessage());
		    Utils.logError(t);
		    requestHandler.onFailure();
		}
	    }

	}.start();

    }

    /**
     * If user does not exist on server : return to Login
     * 
     * @param response
     * @throws IllegalStateException
     * @throws IOException
     */
    protected void checkIfUserHasBeenDeleted(HttpResponse response)
	    throws IllegalStateException, IOException {
	if (response.getStatusLine().getStatusCode() == 500) {
	    if (Utils.inputstreamToString(response.getEntity().getContent())
		    .equals("NoSuchUserException")) {
		TBPreferences.clear();
		AndroidTravelBookActivity.instance
			.startActivity(new Intent(
				AndroidTravelBookActivity.instance,
				LoginActivity.class));
	    }
	}

    }

    public static void removeFollower(final Long followerId,
	    final Long followerUserId) {

	Requester requester = new Requester();
	if (isOnline(false)) {
	    requester.get(new RequestHandler() {

		@Override
		public void onSuccess(InputStream inputStream) {
		    AndroidTravelBookActivity.instance.getFollowersDbAdapter()
			    .deleteFollower(followerId);
		    if (followerUserId != null) {
			getItemsDbAdapter().deleteItems(followerUserId);
		    }
		}

		@Override
		public void onFailure() {
		}

		@Override
		public String getUri() {
		    return "/jsonRemoveFollower?token="
			    + TBPreferences.getToken() + "&id=" + followerId;
		}
	    });
	}
    }

    public static void postFollowers(final List<Follower> locallyAddedFollowers) {

	Requester requester = new Requester();
	if (isOnline(false)) {

	    JSONArray jsonArray = new JSONArray();
	    for (Follower follower : locallyAddedFollowers) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("email", follower.getEmail());
		JSONObject jsonObject = new JSONObject(map);
		jsonArray.put(jsonObject);
	    }

	    try {
		requester.postJSON(new RequestHandler() {

		    @Override
		    public void onSuccess(InputStream inputStream) {
			for (Follower follower : locallyAddedFollowers) {
			    follower.setStatus(null);
			    AndroidTravelBookActivity.instance
				    .getFollowersDbAdapter().updateFollower(
					    follower);
			}
		    }

		    @Override
		    public void onFailure() {
		    }

		    @Override
		    public String getUri() {
			return "/jsonAddFollowers?token="
				+ TBPreferences.getToken();
		    }
		}, jsonArray.toString());
	    } catch (UnsupportedEncodingException e) {
		Utils.logError(e);
	    }
	}
    }

    public static void postItem(final Item item) {
	final Requester requester = new Requester();
	if (isOnline(false)) {

	    Map<String, Object> map = new HashMap<String, Object>();
	    if (item.getId() == null) {
		map.put("id", new Long(0));
	    } else {
		map.put("id", item.getId());
	    }
	    map.put("contentType", item.getContentType());
	    map.put("name", item.getName());
	    map.put("date", Utils.parseDateToJSON(item.getDate()));
	    map.put("lastModified", item.getLastModified());
	    map.put("content", Utils.replaceCRByBR(item.getContent()));
	    map.put("contentDate", Utils.parseDateToJSON(item.getContentDate()));
	    JSONObject jsonObject = new JSONObject(map);

	    try {
		requester.postJSON(new RequestHandler() {

		    @Override
		    public void onSuccess(InputStream inputStream) {
			String result = Utils.inputstreamToString(inputStream);
			if (item.isText()) {
			    item.setId(new Long(result));
			    item.setStatus(null);
			    getItemsDbAdapter().updateItemId(item);
			} else {
			    if (item.isCreated()) {
				startUpload(result);
			    } else if (item.isModified()) {
				item.setStatus(null);
				AndroidTravelBookActivity.instance
					.getItemsDbAdapter().updateItem(item);
			    }
			}
		    }

		    private void startUpload(String blobUploadUrl) {
			RequestHandler requestHandler = new RequestHandler() {

			    @Override
			    public void onSuccess(InputStream inputStream) {
				// Maybe item has changed since the
				// beginning of upload
				Item newItem = AndroidTravelBookActivity.instance
					.getItemsDbAdapter().fetchItem(
						item.getReference());
				if (newItem != null) {
				    String result = Utils
					    .inputstreamToString(inputStream);
				    if (result.startsWith("Quota")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
						ItemsListFragment.instance
							.getActivity());
					builder.setMessage(result)
						.setCancelable(false);
					alert(builder);
				    }
				    if (result.contains(" ")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
						ItemsListFragment.instance
							.getActivity());
					Resources res = ItemsListFragment.instance
						.getResources();
					builder.setMessage(
						String.format(
							res.getString(R.string.item_already_uploaded),
							item.getName()))
						.setCancelable(false);
					alert(builder);
				    } else {
					newItem.setId(new Long(result));
					newItem.setStatus(null);
					AndroidTravelBookActivity.instance
						.getItemsDbAdapter()
						.updateItemId(newItem);
				    }
				} else {
				    AndroidTravelBookActivity.instance
					    .getItemsDbAdapter().deleteItem(
						    item.getReference());
				    Utils.logInfo("Item deleted before upload : "
					    + item.getReference());
				}
			    }

			    private void alert(final Builder builder) {
				AndroidTravelBookActivity.instance
					.runOnUiThread(new Runnable() {
					    @Override
					    public void run() {
						AlertDialog alert = builder
							.create();
						alert.show();

					    }
					});
			    }

			    @Override
			    public void onFailure() {
			    }

			    @Override
			    public String getUri() {
				// Url is generated by Google App Engine
				return null;
			    }
			};
			requester.uploadFile(item, blobUploadUrl,
				requestHandler);
		    }

		    @Override
		    public void onFailure() {
		    }

		    @Override
		    public String getUri() {
			return "/jsonPutItem?token=" + TBPreferences.getToken();
		    }
		}, jsonObject.toString());
	    } catch (UnsupportedEncodingException e) {
		Utils.logError(e);
	    }
	}
    }

    protected static ItemsDbAdapter getItemsDbAdapter() {
	return AndroidTravelBookActivity.instance.getItemsDbAdapter();
    }

    private void uploadFile(final Item item, String blobUploadUrl,
	    RequestHandler requestHandler) {
	try {
	    ++runningUploads;
	    HttpPost httpPost = new HttpPost(blobUploadUrl);
	    MultipartEntity reqEntity = new MultipartEntity(
		    HttpMultipartMode.BROWSER_COMPATIBLE);
	    TBInputStream inputStream = new TBInputStream(item);
	    ContentBody contentBody = new InputStreamBody(inputStream,
		    item.getContentType(), item.getFile().getName());
	    reqEntity.addPart("uploaded", contentBody);
	    httpPost.setEntity(reqEntity);
	    performHttpRequest(httpPost, requestHandler);
	} catch (Throwable t) {
	    Utils.logError(t);
	} finally {
	    --runningUploads;
	}
    }

    public static void deleteItem(final Long itemId) {
	Requester requester = new Requester();
	if (isOnline(false)) {

	    requester.get(new RequestHandler() {

		@Override
		public void onSuccess(InputStream inputStream) {
		    getItemsDbAdapter().deleteItem(itemId);
		}

		@Override
		public void onFailure() {
		}

		@Override
		public String getUri() {
		    return "/jsonDeleteItem?token=" + TBPreferences.getToken()
			    + "&id=" + itemId;
		}
	    });
	}
    }

    public static void downloadItem(final Item item) {
	try {
	    Utils.logInfo("Downloading item " + item.getName());
	    if (isOnline(false)) {

		String uri = SERVER_BASE_URL + item.getReference();

		// Check if download is already running
		for (Item downloadingItem : enqueuedDownloads.values()) {
		    if (downloadingItem.equals(item)) {
			Utils.logInfo("File already downloading : "
				+ item.getName());
			return;
		    }
		}

		// if (item.isIncomplete()) {
		// downloadItemByHand(item);
		// } else {
		Request request = new Request(Uri.parse(uri));
		request.setTitle(item.getName());
		request.setAllowedOverRoaming(false);
		long downloadId = getDownloadManager().enqueue(request);
		++runningDownloads;
		item.setDownloadId(downloadId);
		getItemsDbAdapter().updateItem(item);
		enqueuedDownloads.put(downloadId, item);
		lookAtDownloads();
		// }
	    }
	} catch (Throwable t) {
	    Utils.logError(t);
	}
    }

    public static void initDownloads() {
	alreadyDownloaded = 0;
	if (enqueuedDownloads == null) {
	    enqueuedDownloads = new HashMap<Long, Item>();
	}
    }

    private static void lookAtDownloads() {
	if (timer == null) {
	    timer = new Timer();
	}
	if (!timerIsRunning) {
	    timerIsRunning = true;
	    timer.scheduleAtFixedRate(new TimerTask() {

		@Override
		public void run() {
		    DownloadManager.Query q = new DownloadManager.Query();
		    Cursor cursor = getDownloadManager().query(q);
		    try {
			if (cursor.getCount() > 0) {
			    cursor.moveToFirst();
			    if (cursor.getCount() > 0) {
				int totalDownloaded = 0;
				while (!cursor.isAfterLast()) {
				    long downloadId = cursor.getLong(cursor
					    .getColumnIndex(DownloadManager.COLUMN_ID));
				    Item item = enqueuedDownloads
					    .get(downloadId);
				    if (item != null) {
					long downloadedSoFar = cursor
						.getLong(cursor
							.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
					totalDownloaded = (int) (totalDownloaded + downloadedSoFar);
				    } else {
					getDownloadManager().remove(downloadId);
				    }
				    cursor.moveToNext();
				}
				ItemsListFragment.instance
					.setProgressBar(totalDownloaded
						+ alreadyDownloaded);
			    } else {
				if (checkProgressBar()) {
				    Utils.logInfo("Cancel download progress timer");
				    timerIsRunning = false;
				    cancel();
				}
			    }
			}
		    } finally {
			if (cursor != null) {
			    cursor.close();
			}
		    }
		}
	    }, 0, 1000);
	}
    }

    protected static boolean checkProgressBar() {
	if (runningUploads <= 0 && runningDownloads <= 0) {
	    Utils.logInfo("checkProgressBar hideProgressBar");
	    ItemsListFragment.instance.hideProgressBar();
	    return true;
	} else {
	    return false;
	}
    }

    private static DownloadManager getDownloadManager() {
	if (downloadManager == null) {
	    downloadManager = (DownloadManager) AndroidTravelBookActivity.instance
		    .getSystemService(Context.DOWNLOAD_SERVICE);
	    BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
		    String action = intent.getAction();
		    if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
			receivedFile(intent.getLongExtra(
				DownloadManager.EXTRA_DOWNLOAD_ID, 0));

		    }

		}
	    };
	    AndroidTravelBookActivity.instance.registerReceiver(receiver,
		    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}
	return downloadManager;
    }

    protected static void receivedFile(long downloadId) {
	Item item = enqueuedDownloads.get(downloadId);
	if (item == null) {
	    Utils.logError("Item not found for download task : " + downloadId);
	    return;
	}
	try {
	    Query query = new Query().setFilterById(downloadId);
	    Cursor cursor = downloadManager.query(query);
	    try {
		if (cursor.moveToFirst()) {
		    int status = cursor.getInt(cursor
			    .getColumnIndex(DownloadManager.COLUMN_STATUS));
		    if (DownloadManager.STATUS_SUCCESSFUL == status) {
			if (saveFile(item, cursor)) {
			    downloadSuccess(item);
			}
		    } else {
			// Download failure
			String reason = cursor.getString(cursor
				.getColumnIndex(DownloadManager.COLUMN_REASON));
			int downloadedSoFar = cursor
				.getInt(cursor
					.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
			Utils.logError("DonwloadManager failed with item "
				+ item.getName() + " status=" + status
				+ " reason=" + reason + " downloadedSoFar="
				+ downloadedSoFar + " file length="
				+ item.getLength());
			ItemsListFragment.instance.downloadFailure(item
				.getName());
			// downloadItemByHand(item);
		    }
		}
	    } finally {
		if (cursor != null) {
		    cursor.close();
		}
	    }
	} catch (Throwable t) {
	    Utils.logError(t);
	} finally {
	    enqueuedDownloads.remove(new Long(downloadId));
	    getDownloadManager().remove(downloadId);
	    alreadyDownloaded = alreadyDownloaded + item.getLength().intValue();
	    --runningDownloads;
	    checkProgressBar();
	}
    }

    private static boolean saveFile(Item item, Cursor cursor) {
	String uriString = cursor.getString(cursor
		.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
	InputStream inputStream = null;
	FileOutputStream fileOutputStream = null;
	try {
	    inputStream = AndroidTravelBookActivity.instance
		    .getContentResolver().openInputStream(Uri.parse(uriString));
	    fileOutputStream = FileStorage.getInstance()
		    .getItemFileOutputStream(item, false);
	    byte buffer[] = new byte[1024];
	    int count;
	    while ((count = inputStream.read(buffer)) != -1) {
		fileOutputStream.write(buffer, 0, count);
	    }
	    return true;
	} catch (Throwable t) {
	    Utils.logError(t);
	    FileStorage.getInstance().deleteFiles(item);
	    ItemsListFragment.instance.downloadFailure(item.getName());
	    // Will retry at next refresh
	} finally {
	    try {
		if (inputStream != null) {
		    inputStream.close();
		}
		if (fileOutputStream != null) {
		    fileOutputStream.flush();
		    fileOutputStream.close();
		}
	    } catch (Throwable t) {
		Utils.logError(t);
	    }
	}
	return false;
    }

    private static void downloadSuccess(Item item) {
	item.setStatus(null);
	item.setLibraryPath(null);
	Utils.createThumbnail(item);
	getItemsDbAdapter().updateItem(item);
    }

    private static void downloadItemByHand(final Item item) {
	try {
	    Requester requester = new Requester();
	    if (isOnline(false)) {

		FileOutputStream fileOutputStream = FileStorage.getInstance()
			.getItemFileOutputStream(item, true);
		requester.downloadByHand(new RequestHandler() {

		    @Override
		    public void onSuccess(InputStream inputStream) {
			downloadSuccess(item);
		    }

		    @Override
		    public void onFailure() {
		    }

		    @Override
		    public String getUri() {
			return item.getReference();
		    }
		}, item, fileOutputStream);
	    }
	} catch (Throwable t) {
	    Utils.logError(t);
	}
    }

    /**
     * Download a file from the server
     * 
     * @param requestHandler
     * @param item
     * @param fileOutputStream
     */
    public void downloadByHand(final RequestHandler requestHandler,
	    final Item item, final FileOutputStream fileOutputStream) {
	if (isOnline(false)) {
	    new Thread() {

		@Override
		public void run() {
		    Utils.logInfo("downloadByHand " + item.getName()
			    + " length=" + item.getFile().length()
			    + " item length=" + item.getLength() + "");
		    int MAX_BUFFER_SIZE = 1024;
		    int receivedBytes = 0;
		    InputStream inputStream = null;
		    TBListFragment listActivity = ItemsListFragment.instance;
		    String target = SERVER_BASE_URL + requestHandler.getUri();
		    int retry = 3;
		    boolean downloaded = false;
		    while (!downloaded && retry > 0) {
			Utils.logInfo(item.getName() + " receivedBytes="
				+ receivedBytes + " retry=" + retry);
			--retry;
			try {
			    URL url = new URL(target);
			    URLConnection connection = url.openConnection();
			    connection.addRequestProperty("Range", "bytes="
				    + receivedBytes + "-");
			    connection.connect();
			    inputStream = new BufferedInputStream(
				    url.openStream());
			    byte buffer[] = new byte[MAX_BUFFER_SIZE];
			    int count;
			    while ((count = inputStream.read(buffer)) != -1) {
				fileOutputStream.write(buffer, 0, count);
				receivedBytes = receivedBytes + count;
				if (listActivity != null) {
				    listActivity.increaseProgressBar(count);
				}
			    }
			    requestHandler.onSuccess(null);
			    downloaded = true;
			} catch (Exception e) {
			    if (retry > 0) {
				Utils.logError("Request " + target
					+ " failed retry=" + retry, e);
			    } else {
				Utils.logError(
					"Request " + target + " failed.", e);
				requestHandler.onFailure();
			    }
			} finally {
			    try {
				// Close inputstream in all cases
				if (inputStream != null) {
				    inputStream.close();
				}
				// Close outputstream at the end
				if (fileOutputStream != null
					&& (downloaded || retry < 0)) {
				    fileOutputStream.flush();
				    fileOutputStream.close();
				}
			    } catch (IOException e) {
				Utils.logError(e);
			    }
			    if (retry < 0) {
				// Last chance
				if (listActivity != null
					&& receivedBytes != item.getLength()
						.intValue()) {
				    listActivity.increaseProgressBar(item
					    .getLength().intValue()
					    - receivedBytes);
				}
			    }
			}
		    }
		}

	    }.start();
	} else {
	    requestHandler.onFailure();
	}

    }

    public static void getItems() {
	Requester requester = new Requester();
	if (isOnline(true)) {
	    isSynchronizing = true;
	    requester.get(new RequestHandler() {

		@Override
		public String getUri() {
		    return "/jsonGetItems?token=" + TBPreferences.getToken();
		}

		@Override
		public void onSuccess(InputStream inputStream) {
		    try {
			Synchronizer synchronizer = new Synchronizer();
			synchronizer.synchronize(new JSONObject(Utils
				.inputstreamToString(inputStream)));
		    } catch (Throwable t) {
			Utils.logError(t);
		    } finally {
			isSynchronizing = false;
		    }
		}

		@Override
		public void onFailure() {
		    Utils.logInfo("OnFailure hideProgressBar");
		    ItemsListFragment.instance.hideProgressBar();
		    isSynchronizing = false;
		}
	    });
	} else {
	    Utils.logInfo("Network unreachable hideProgressBar");
	    ItemsListFragment.instance.hideProgressBar();
	}

    }

    public static void setConnectivityManager(ConnectivityManager cm) {
	connectivityManager = cm;
    }

    public static boolean changeReceptionAlbumKind(final Context context,
	    final String kind) {
	Requester requester = new Requester();
	if (isOnline(true)) {
	    requester.get(new RequestHandler() {

		@Override
		public String getUri() {
		    return "/jsonChangeReceptionAlbumKind?token="
			    + TBPreferences.getToken() + "&kind=" + kind;
		}

		@Override
		public void onSuccess(InputStream inputStream) {
		    try {
			String kind = Utils.inputstreamToString(inputStream);
			TBPreferences.setReceptionAlbumKind(kind);
		    } catch (Throwable t) {
			Utils.logError(t);
		    }
		}

		@Override
		public void onFailure() {
		    Utils.toast(
			    context,
			    context.getResources().getString(
				    R.string.network_error));
		}
	    });
	    return true;
	} else {
	    return false;
	}

    }

    public static void archiveReceptionAlbum(String name, String year,
	    String kind, String description) {
	final Requester requester = new Requester();
	if (isOnline(true)) {

	    Map<String, Object> map = new HashMap<String, Object>();
	    map.put("name", name);
	    map.put("year", year);
	    map.put("kind", kind);
	    map.put("description", description);
	    map.put("date", Utils.parseDateToJSON(new Date()));
	    JSONObject jsonObject = new JSONObject(map);

	    try {
		requester.postJSON(new RequestHandler() {

		    @Override
		    public void onSuccess(InputStream inputStream) {
			getItems();
		    }

		    @Override
		    public void onFailure() {
		    }

		    @Override
		    public String getUri() {
			return "/jsonArchiveReceptionAlbum?token="
				+ TBPreferences.getToken();
		    }
		}, jsonObject.toString());
	    } catch (UnsupportedEncodingException e) {
		Utils.logError(e);
	    }
	}
    }

    public static void flushDownloadManager() {
	try {
	    DownloadManager downloadManager = (DownloadManager) AndroidTravelBookActivity.instance
		    .getSystemService(Context.DOWNLOAD_SERVICE);
	    if (downloadManager != null) {
		DownloadManager.Query q = new DownloadManager.Query();
		Cursor cursor = getDownloadManager().query(q);
		try {
		    cursor.moveToFirst();
		    while (!cursor.isAfterLast()) {
			long downloadId = cursor.getLong(cursor
				.getColumnIndex(DownloadManager.COLUMN_ID));
			downloadManager.remove(downloadId);
			cursor.moveToNext();
		    }
		} finally {
		    if (cursor != null) {
			cursor.close();
		    }
		}
	    }
	} catch (Throwable t) {
	    Utils.logError(t);
	}

    }

}
