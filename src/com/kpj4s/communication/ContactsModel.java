package com.kpj4s.communication;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.ContactsContract;

public class ContactsModel extends ContentObserver {
	Application application;
	private List<Model> list;

	public ContactsModel(Application application) {
		super(new Handler());
		this.application = application;
	}

	public List<Model> updateContactList() {
		list  = new ArrayList<Model>();
		String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
		Cursor phones = application.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
				null, sortOrder);
		while (phones.moveToNext()) {

			String contactName = phones
					.getString(phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			if (contactName == null || contactName.length() <= 0) {
				continue;
			}
			
			String phoneNumber = phones
					.getString(phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			if (phoneNumber == null || phoneNumber.length() <= 0) {
				continue;
			}
			
			String numberType = phones
					.getString(phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
			
			String mType;
			if ("0".equals(numberType))
				mType = "Work";
			else if ("1".equals(numberType))
				mType = "Home";
			else if ("2".equals(numberType))
				mType = "Mobile";
			else
				mType = "Other";

			list.add(getModel(contactName + "\n<"
					+ extractRecipientNumber(phoneNumber) + ">\t" + mType));
		}
		phones.close();
		return list;
	}
	
	private String extractRecipientNumber(String phoneNumber) {
		String mNumber = phoneNumber.replaceAll("\\D+", "");
		int mNumberLen = mNumber.length();
		try {
			return mNumber.substring(mNumberLen - 10, mNumberLen);
		} catch (StringIndexOutOfBoundsException e) {
			return mNumber;
		}
	}
	
	public List<Model> getContactList() {
		return list;
	}

	private Model getModel(String s) {
		return new Model(s);
	}
}
