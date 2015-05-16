package com.bcdlog.travelbook.database;

import java.io.File;
import java.util.Date;

import com.bcdlog.travelbook.Utils;

public class Item extends Bean {

    public static final int TEXT = 0;
    public static final int IMAGE = 1;
    public static final int VIDEO = 2;
    public static final int SOUND = 3;
    public static final int UNKNOWN = 4;

    private String content;
    private Date contentDate;
    private String contentType;
    private Date date;
    private String duration;
    private Long height;
    private Long lastModified;
    private Double latitude;
    private Long length;
    private String libraryPath;
    private Double longitude;
    private String name;
    private String path;
    private String reference;
    private String status;
    private Long userId;
    private Long width;
    private byte[] thumbnail;
    private Long downloadId;

    /**
     * For gsallery view reuse
     */
    private int type;

    /**
     * @return the content
     */
    public String getContent() {
	return content;
    }

    /**
     * @param content
     *            the content to set
     */
    public void setContent(String content) {
	this.content = content;
    }

    /**
     * @return the contentDate
     */
    public Date getContentDate() {
	return contentDate;
    }

    /**
     * @param contentDate
     *            the contentDate to set
     */
    public void setContentDate(Date contentDate) {
	this.contentDate = contentDate;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
	return contentType;
    }

    /**
     * @param contentType
     *            the contentType to set
     */
    public void setContentType(String contentType) {
	if (contentType == null) {
	    contentType = Utils.mimeTypeFromFilname(getName());
	}
	this.contentType = contentType;
	if (isText()) {
	    setType(TEXT);
	} else if (isImage()) {
	    setType(IMAGE);
	} else if (isVideo()) {
	    setType(VIDEO);
	} else if (isAudio()) {
	    setType(SOUND);
	} else {
	    setType(UNKNOWN);
	}
    }

    /**
     * @return the date
     */
    public Date getDate() {
	return date;
    }

    /**
     * @param date
     *            the date to set
     */
    public void setDate(Date date) {
	this.date = date;
    }

    /**
     * @return the duration
     */
    public String getDuration() {
	return duration;
    }

    /**
     * @param duration
     *            the duration to set
     */
    public void setDuration(String duration) {
	this.duration = duration;
    }

    /**
     * @return the height
     */
    public Long getHeight() {
	return height;
    }

    /**
     * @param height
     *            the height to set
     */
    public void setHeight(Long height) {
	this.height = height;
    }

    /**
     * @return the lastModified
     */
    public Long getLastModified() {
	return lastModified;
    }

    /**
     * @param lastModified
     *            the lastModified to set
     */
    public void setLastModified(Long lastModified) {
	this.lastModified = lastModified;
    }

    /**
     * @return the latitude
     */
    public Double getLatitude() {
	return latitude;
    }

    /**
     * @param latitude
     *            the latitude to set
     */
    public void setLatitude(Double latitude) {
	this.latitude = latitude;
    }

    /**
     * @return the length
     */
    public Long getLength() {
	return length;
    }

    /**
     * @param length
     *            the length to set
     */
    public void setLength(Long length) {
	this.length = length;
    }

    /**
     * @return the libraryPath
     */
    public String getLibraryPath() {
	return libraryPath;
    }

    /**
     * @param libraryPath
     *            the libraryPath to set
     */
    public void setLibraryPath(String libraryPath) {
	this.libraryPath = libraryPath;
    }

    /**
     * @return the longitude
     */
    public Double getLongitude() {
	return longitude;
    }

    /**
     * @param longitude
     *            the longitude to set
     */
    public void setLongitude(Double longitude) {
	this.longitude = longitude;
    }

    /**
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * @return the path
     */
    public String getPath() {
	return path;
    }

    /**
     * @param path
     *            the path to set
     */
    public void setPath(String path) {
	this.path = path;
    }

    /**
     * @return the reference
     */
    public String getReference() {
	return reference;
    }

    /**
     * @param reference
     *            the reference to set
     */
    public void setReference(String reference) {
	this.reference = reference;
    }

    /**
     * @return the status
     */
    public String getStatus() {
	return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
	this.status = status;
    }

    /**
     * @return the userId
     */
    public Long getUserId() {
	return userId;
    }

    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId(Long userId) {
	this.userId = userId;
    }

    /**
     * @return the width
     */
    public Long getWidth() {
	return width;
    }

    /**
     * @param width
     *            the width to set
     */
    public void setWidth(Long width) {
	this.width = width;
    }

    @Override
    public String toString() {
	return getId() + " " + content + " " + contentDate + " " + contentType
		+ " " + date + " " + duration + " " + height + " "
		+ lastModified + " " + latitude + " " + length + " "
		+ libraryPath + " " + longitude + " " + name + " " + path + " "
		+ reference + " " + status + " " + userId + " " + width;
    }

    public boolean isModified() {
	return getStatus() != null
		&& getStatus().toString()
			.equals(ItemsDbAdapter.Status.MODIFIED);
    }

    public void setModified() {
	if (!isCreated()) {
	    setStatus(ItemsDbAdapter.Status.MODIFIED.toString());
	}
    }

    public boolean isText() {
	return getContentType() != null && getContentType().startsWith("text");
    }

    public boolean isAudio() {
	return getContentType() != null && getContentType().startsWith("audio");
    }

    public boolean isVideo() {
	return getContentType() != null && getContentType().startsWith("video");
    }

    public boolean isImage() {
	return getContentType() != null && getContentType().startsWith("image");
    }

    public File getFile() {
	File file = null;
	if (getLibraryPath() != null) {
	    file = new File(getLibraryPath());
	} else if (getPath() != null) {
	    file = new File(getPath());
	}
	if (file != null && file.exists()) {
	    return file;
	} else {
	    return null;
	}
    }

    public boolean needsToBeDownloaded() {
	return !isText()
		&& getStatus() != null
		&& getStatus().equals(
			ItemsDbAdapter.Status.TO_BE_DOWNLOADED.toString());
    }

    public boolean isCreated() {
	return getStatus() != null
		&& getStatus().equals(ItemsDbAdapter.Status.CREATED.toString());
    }

    public boolean isDeleted() {
	return getStatus() != null
		&& getStatus().equals(ItemsDbAdapter.Status.DELETED.toString());
    }

    @Override
    public boolean equals(Object o) {
	try {
	    Item other = (Item) o;
	    return other.getId().equals(getId());
	} catch (Throwable t) {
	    return false;
	}
    }

    public File getDirectory() {
	if (getPath() != null) {
	    File file = new File(getPath());
	    if (file != null) {
		return file.getParentFile();
	    }
	}
	return null;
    }

    /**
     * @return the type
     */
    public int getType() {
	return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(int type) {
	this.type = type;
    }

    public byte[] getThumbnail() {
	// File file = getFile();
	// if (!isText() && file == null && getId().longValue() > 0) {
	// setStatus(ItemsDbAdapter.Status.TO_BE_DOWNLOADED.toString());
	// ko: AndroidTravelBookActivity.instance.getItemsDbAdapter()
	// .updateItem(this);
	// Requester.downloadItem(this);
	// }
	return thumbnail;
    }

    /**
     * @param thumbnail
     *            the thumbnail to set
     */
    public void setThumbnail(byte[] thumbnail) {
	this.thumbnail = thumbnail;
    }

    /**
     * @return the downloadId
     */
    public Long getDownloadId() {
	return downloadId;
    }

    /**
     * @param downloadId
     *            the downloadId to set
     */
    public void setDownloadId(Long downloadId) {
	this.downloadId = downloadId;
    }

    public boolean isIncomplete() {
	try {
	    if (!isText() && getLength() != null) {
		File file = getFile();
		if (file != null && file.length() != getLength().longValue()) {
		    return true;
		}
	    }
	} catch (Throwable t) {
	    Utils.logError(t);
	}
	return false;
    }

}
