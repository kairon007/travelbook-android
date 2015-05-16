package com.bcdlog.travelbook;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bcdlog.travelbook.activities.AndroidTravelBookActivity;
import com.bcdlog.travelbook.database.Item;

public class Utils {

    private static final String DB_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String FILE_NAME_DATE_FORMAT = "yyyyMMddHHmmss";
    private static final String JSON_DATE_FORMAT = "MMM d, y h:m:s a";
    private static final String US_DATE_FORMAT = "MM/dd/yyyy hh:mm a";
    // private static final String FRENCH_DATE_FORMAT =
    // "EEEE dd MMMM yyyy HH:mm";
    private static final String FRENCH_DATE_FORMAT = "dd/MM/yyyy HH:mm";

    private static Properties mimeTypes;

    static public String inputstreamToString(InputStream inputStream) {
	BufferedReader reader = new BufferedReader(new InputStreamReader(
		inputStream));
	StringBuilder builder = new StringBuilder();
	String line;
	try {
	    while ((line = reader.readLine()) != null) {
		builder.append(line);
	    }
	} catch (IOException e) {
	    logError(e);
	} finally {
	    if (inputStream != null) {
		try {
		    inputStream.close();
		} catch (IOException e) {
		    logError(e);
		}
	    }
	}
	return builder.toString();
    }

    static public void logError(Throwable t) {
	Log.e("", "" + t.getMessage(), t);
    }

    static public void logError(String message, Throwable t) {
	Log.e("", "" + message, t);
    }

    public static void logError(String message) {
	Log.e("", "" + message);
    }

    public static void logInfo(String message) {
	String fullClassName = Thread.currentThread().getStackTrace()[3]
		.getClassName();
	String className = fullClassName.substring(fullClassName
		.lastIndexOf(".") + 1);
	String methodName = Thread.currentThread().getStackTrace()[3]
		.getMethodName();
	Log.i(className + "." + methodName, "" + message);
    }

    /**
     * DB : YYYY-MM-DD HH:MM:SS.SSS
     * 
     * @param dbDate
     * @return
     */
    public static Date parseDateFomDB(String dbDate) {
	try {
	    SimpleDateFormat dateFormat = new SimpleDateFormat(DB_DATE_FORMAT);
	    return dateFormat.parse(dbDate);
	} catch (Throwable t) {
	    return null;
	}
    }

    public static String parseDateToDB(Date contentDate) {
	try {
	    SimpleDateFormat dateFormat = new SimpleDateFormat(DB_DATE_FORMAT);
	    return dateFormat.format(contentDate);
	} catch (Throwable t) {
	    return null;
	}
    }

    /**
     * Feb 10, 2012 3:44:33 PM
     * 
     * @param jsonDate
     * @return
     */
    public static Date parseDateFomJSON(String jsonDate) {
	try {
	    SimpleDateFormat dateFormat = new SimpleDateFormat(
		    JSON_DATE_FORMAT, Locale.US);
	    return dateFormat.parse(jsonDate);
	} catch (Throwable t) {
	    return null;
	}
    }

    public static String parseDateToJSON(Date date) {
	try {
	    SimpleDateFormat dateFormat = new SimpleDateFormat(
		    FILE_NAME_DATE_FORMAT, Locale.US);
	    return dateFormat.format(date);
	} catch (Throwable t) {
	    return null;
	}
    }

    public static String replaceBRByCR(String string) {
	if (string != null) {
	    return string.replaceAll("<br>", "\n");
	} else {
	    return "";
	}
    }

    public static String replaceCRByBR(String string) {
	if (string != null) {
	    return string.replaceAll("\\n", "<br>");
	} else {
	    return "";
	}
    }

    public static Date now() {
	SimpleDateFormat dateFormat = new SimpleDateFormat(
		FILE_NAME_DATE_FORMAT);
	String date = dateFormat.format(new Date());
	dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	try {
	    return dateFormat.parse(date);
	} catch (ParseException e) {
	    Utils.logError(e);
	}
	return null;
    }

    public static int getScreenWidth() {
	return AndroidTravelBookActivity.instance.getWindowManager()
		.getDefaultDisplay().getWidth();
    }

    public static int getScreenHeight() {
	return AndroidTravelBookActivity.instance.getWindowManager()
		.getDefaultDisplay().getHeight();
    }

    public static void createIcon(Item item, ImageView icon) {

	try {
	    icon.setContentDescription(item.getContentType());
	    if (item.isImage()) {
		createIconFromThumbnail(item, icon);
	    } else if (item.isVideo()) {
		try {
		    createIconFromThumbnail(item, icon);
		} catch (Throwable t) {
		    icon.setImageResource(R.drawable.video_icon);
		}
	    } else if (item.isAudio()) {
		icon.setImageResource(R.drawable.audio_icon);
	    } else {
		icon.setImageResource(R.drawable.unknown_icon);
	    }
	} catch (Throwable t) {
	    icon.setImageResource(R.drawable.unknown_icon);
	}

    }

    private static void createIconFromThumbnail(Item item, ImageView imageView) {
	byte[] thumbnail = item.getThumbnail();
	Bitmap imageBitmap = BitmapFactory.decodeByteArray(thumbnail, 0,
		thumbnail.length);
	imageView.setImageBitmap(imageBitmap);
    }

    public static void createThumbnail(Item item) {
	try {
	    if (item.isImage()) {
		File file = item.getFile();
		Options options = new Options();
		options.inSampleSize = 4;
		Bitmap imageBitmap = BitmapFactory.decodeFile(
			file.getAbsolutePath(), options);
		Float width = new Float(imageBitmap.getWidth());
		Float height = new Float(imageBitmap.getHeight());
		Float ratio = width / height;
		int thumbnailWidth = AndroidTravelBookActivity.instance
			.getResources().getDimensionPixelSize(
				R.dimen.thumbnail_width);
		int thumbnailHeigth = AndroidTravelBookActivity.instance
			.getResources().getDimensionPixelSize(
				R.dimen.thumbnail_heigth);
		imageBitmap = Bitmap.createScaledBitmap(imageBitmap,
			(int) (thumbnailWidth * ratio), thumbnailHeigth, false);
		createThumbnail(item, imageBitmap);
	    } else if (item.isVideo()) {
		Bitmap imageBitmap = ThumbnailUtils.createVideoThumbnail(item
			.getFile().getAbsolutePath(),
			MediaStore.Images.Thumbnails.MINI_KIND);
		Bitmap mutableBitmap = imageBitmap.copy(
			Bitmap.Config.ARGB_8888, true);
		Bitmap videoOverlay = BitmapFactory.decodeResource(
			AndroidTravelBookActivity.instance.getResources(),
			R.drawable.video_overlay);
		Canvas canvas = new Canvas(mutableBitmap);
		float left = 20;
		if (mutableBitmap.getWidth() > videoOverlay.getWidth()) {
		    left = (mutableBitmap.getWidth() - videoOverlay.getWidth()) / 2;
		}
		float top = 20;
		if (mutableBitmap.getHeight() > videoOverlay.getHeight()) {
		    top = (mutableBitmap.getHeight() - videoOverlay.getHeight()) / 2;
		}
		canvas.drawBitmap(videoOverlay, left, top, null);
		createThumbnail(item, mutableBitmap);
	    }
	} catch (Throwable t) {
	    Utils.logError(t);
	}
    }

    private static void createThumbnail(Item item, Bitmap imageBitmap) {
	if (imageBitmap != null) {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
	    item.setThumbnail(baos.toByteArray());
	} else {
	    logError("Failed to create thumbnail for item "
		    + item.getReference());
	}
    }

    public static String extractFileExtension(String filePath) {
	if (filePath.contains(".")) {
	    return filePath.substring(filePath.lastIndexOf(".") + 1);
	}
	return "";
    }

    public static void toast(Context context, String message) {
	int duration = Toast.LENGTH_LONG;
	Toast toast = Toast.makeText(context, message, duration);
	toast.show();
    }

    public static String prettyDate(String string) {
	Date date = parseDateFomDB(string);
	String pattern = US_DATE_FORMAT;
	if (Locale.getDefault().getLanguage().equals("fr")) {
	    pattern = FRENCH_DATE_FORMAT;
	}
	SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
	return dateFormat.format(date);
    }

    public static boolean isDebuggable() {
	boolean debuggable = false;

	PackageManager pm = AndroidTravelBookActivity.instance
		.getPackageManager();
	try {
	    ApplicationInfo appinfo = pm.getApplicationInfo(
		    AndroidTravelBookActivity.instance.getPackageName(), 0);
	    debuggable = (0 != (appinfo.flags &= ApplicationInfo.FLAG_DEBUGGABLE));
	} catch (NameNotFoundException e) {
	    /* debuggable variable will remain false */
	}

	return debuggable;
    }

    public static String mimeTypeFromFilname(String filename) {
	try {
	    if (mimeTypes == null) {
		InputStream inputStream = AndroidTravelBookActivity.instance
			.getResources().getAssets()
			.open("mime_types.properties");
		mimeTypes = new Properties();
		mimeTypes.load(inputStream);
	    }
	    String extension = filename
		    .substring(filename.lastIndexOf(".") + 1);
	    String mimeType = mimeTypes.getProperty(extension.toLowerCase());
	    if (mimeType != null) {
		return mimeType;
	    }
	} catch (Throwable t) {
	    Utils.logError(t);
	}
	return "application/octect-stream";
    }

}
