package com.kpj4s.communication;

import android.app.Application;

public class ContactModelFactory {

	public static ContactsModel contactModel;

	public static void intializeContactModel(Application application) {
		if (contactModel != null)
			return;
		contactModel = new ContactsModel(application);
	}

	public static ContactsModel getContactModel() {
		return contactModel;

	}

}
