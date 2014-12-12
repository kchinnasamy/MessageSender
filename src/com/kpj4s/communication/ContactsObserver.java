package com.kpj4s.communication;

import android.app.Application;
import android.database.ContentObserver;
import android.os.Handler;
import android.widget.ArrayAdapter;

public class ContactsObserver extends ContentObserver {
	private final ArrayAdapter<Model> adapter;
	private final ContactsModel model;

	public ContactsObserver(Application application,
			ArrayAdapter<Model> adapter, ContactsModel model) {
		super(new Handler());
		this.adapter = adapter;
		this.model = model;
		application
				.getApplicationContext()
				.getContentResolver()
				.registerContentObserver(
						android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						true, this);
	}

	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		model.updateContactList();
		adapter.clear();
		for (Model m : model.getContactList())
			adapter.add(m);
	}
}
