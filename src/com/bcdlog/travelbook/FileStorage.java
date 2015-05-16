package com.bcdlog.travelbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;

import com.bcdlog.travelbook.activities.AndroidTravelBookActivity;
import com.bcdlog.travelbook.database.Item;

public class FileStorage {

    private static FileStorage instance;
    private static Object singletonLock = new Object();

    public static FileStorage getInstance() {
	if (instance == null) {
	    synchronized (singletonLock) {
		if (instance == null) {
		    instance = new FileStorage();
		}
	    }
	}
	return instance;
    }

    private FileStorage() {

    }

    public FileOutputStream getItemFileOutputStream(Item item, boolean append)
	    throws IOException {
	FileOutputStream fos = null;
	if (item.getPath() == null) {
	    fos = createFile(item);
	} else {
	    try {
		fos = new FileOutputStream(new File(item.getPath()), append);
	    } catch (FileNotFoundException f) {
		// SD has been removed or file deleted : Download again
		fos = createFile(item);
	    }
	}
	return fos;
    }

    private FileOutputStream createFile(Item item) {
	try {
	    File file = new File(getStorageDir(item), item.getName());
	    // file.createNewFile();
	    item.setPath(file.getAbsolutePath());
	    return new FileOutputStream(file);
	} catch (Throwable t) {
	    Utils.logError(t);
	}
	return null;
    }

    /**
     * Create dir <storage>/itemName or <storage>/itemId
     * 
     * @param item
     * @return
     */
    private File getStorageDir(Item item) {
	File dir = null;
	if (Environment.getExternalStorageState().equals(
		Environment.MEDIA_MOUNTED)) {
	    dir = AndroidTravelBookActivity.instance
		    .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
	    if (dir != null) {
		dir = new File(dir, "travelbook");
	    }
	}
	if (dir == null) {
	    dir = AndroidTravelBookActivity.instance.getDir("datas",
		    Context.MODE_PRIVATE);
	}
	if (dir == null) {
	    throw new RuntimeException("Cannot access file system.");
	}
	if (item.getId() == null) {
	    dir = new File(dir, item.getName());
	} else {
	    dir = new File(dir, String.valueOf(item.getId()));
	}
	dir.mkdirs();
	return dir;
    }

    public void deleteFiles(Item item) {
	if (item != null) {
	    File directory = item.getDirectory();
	    if (directory != null) {
		File[] files = directory.listFiles();
		if (files != null) {
		    for (File file : files) {
			file.delete();
		    }
		}
		directory.delete();
	    }
	}
    }

    public void moveItemToId(Item item) {
	if (item.getPath() == null) {
	    // Not for picked items
	    return;
	}
	File file = item.getFile();
	if (file != null) {
	    File newDirectory = new File(file.getParentFile().getParentFile(),
		    String.valueOf(item.getId()));
	    newDirectory.mkdir();
	    File newPath = new File(newDirectory, file.getName());
	    if (file.renameTo(newPath)) {
		item.setPath(newPath.getAbsolutePath());
	    }
	}
    }

    public void changeFileExtension(Item item, String extension) {
	File file = item.getFile();
	if (file != null) {
	    File newPath = new File(file.getParentFile(), file.getName()
		    .replace(AndroidTravelBookActivity.DEFAULT_EXTENSION,
			    extension));
	    if (file.renameTo(newPath)) {
		item.setPath(newPath.getAbsolutePath());
	    }
	}
    }

}
