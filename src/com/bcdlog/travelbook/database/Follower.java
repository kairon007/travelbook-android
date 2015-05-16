package com.bcdlog.travelbook.database;

public class Follower extends Bean {

    private String aliases;
    private String contact;
    private String email;
    private Long items;
    private String nickname;
    private String status;
    private Long userId;

    /**
     * @return the aliases
     */
    public String getAliases() {
	return aliases;
    }

    /**
     * @param aliases
     *            the aliases to set
     */
    public void setAliases(String aliases) {
	this.aliases = aliases;
    }

    /**
     * @return the contact
     */
    public String getContact() {
	return contact;
    }

    /**
     * @param contact
     *            the contact to set
     */
    public void setContact(String contact) {
	this.contact = contact;
    }

    /**
     * @return the email
     */
    public String getEmail() {
	return email;
    }

    /**
     * @param email
     *            the email to set
     */
    public void setEmail(String email) {
	this.email = email;
    }

    /**
     * @return the items
     */
    public Long getItems() {
	return items;
    }

    /**
     * @param items
     *            the items to set
     */
    public void setItems(Long items) {
	this.items = items;
    }

    /**
     * @return the nickname
     */
    public String getNickname() {
	return nickname;
    }

    /**
     * @param nickname
     *            the nickname to set
     */
    public void setNickname(String nickname) {
	this.nickname = nickname;
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

    public boolean isLocallyAdded() {
	return getStatus() != null
		&& getStatus().equals(
			FollowersDbAdapter.Status.LOCALLY_ADDED.toString());
    }

    public boolean isDeleted() {
	return getStatus() != null
		&& getStatus().equals(
			FollowersDbAdapter.Status.DELETED.toString());
    }

    @Override
    public String toString() {
	return getId() + " " + aliases + " " + contact + " " + email + " "
		+ items + " " + nickname + " " + status + " " + userId;
    }

    @Override
    public boolean equals(Object o) {
	try {
	    Follower other = (Follower) o;
	    return other.getEmail().equals(getEmail());
	} catch (Throwable t) {
	    return false;
	}
    }

    public Object getName() {
	if (getContact() != null) {
	    return getContact() + " ( " + getEmail() + " )";
	} else if (getNickname() != null) {
	    return getNickname() + " ( " + getEmail() + " )";
	} else {
	    return getEmail();
	}
    }
}
