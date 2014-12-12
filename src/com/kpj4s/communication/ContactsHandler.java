package com.kpj4s.communication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;

public class ContactsHandler {
	private static final int SELECTED = 0;
	MultiAutoCompleteTextView selectedContacts;
	final Activity activity;
	public ContactsHandler(final MultiAutoCompleteTextView selectedContacts,
			Button buttonContacts , final Activity activity) {
		this.selectedContacts = selectedContacts;
		this.activity = activity;
		selectedContacts
				.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
		selectedContacts.setThreshold(1);
		selectedContacts.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {

				String mTemp = selectedContacts.getText().toString();
				mTemp = mTemp.replaceAll("\\\n", "");
				String[] splitTemp = mTemp.split("\\\t");
				selectedContacts.setText(splitTemp[0] + ",");
				selectedContacts.setSelection(selectedContacts
						.length());

			}
		});

		buttonContacts.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				launchContacts();
			}
		});

	}
	
	protected void launchContacts() {
		Intent contacts = new Intent(activity, ContactList.class);
		Bundle bundle = new Bundle();
		
		bundle.putString("mrecipients", selectedContacts.getText()
				.toString());
		bundle.putString("msgGateway", this.getClass().getName());
		contacts.putExtras(bundle);
		activity.startActivityForResult(contacts, SELECTED);
	}
}
