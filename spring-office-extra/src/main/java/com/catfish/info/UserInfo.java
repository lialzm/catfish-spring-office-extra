package com.catfish.info;

import java.io.Serializable;

public class UserInfo implements Serializable {

	private String currentUserId;
	private String currentUserName;
	private String mail;

	public String getCurrentUserId() {
		return currentUserId;
	}

	public void setCurrentUserId(String currentUserId) {
		this.currentUserId = currentUserId;
	}

	public String getCurrentUserName() {
		return currentUserName;
	}

	public void setCurrentUserName(String currentUserName) {
		this.currentUserName = currentUserName;
	}


	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	@Override
	public String toString() {
		return "UserInfo [currentUserId=" + currentUserId + ", currentUserName=" + currentUserName + ", userLoginName="
				 + ", mail=" + mail + "]";
	}
	
	

}
