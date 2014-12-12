package com.kpj4s.communication;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.kpj4s.way2sms.R;

public class ContactList extends Activity {

	private ContactArrayAdapter adapter;
	private List<Model> list;
	private String recipients = "";
	private String gatewayName;
	private ListView listview_contact;
	private String savedRecipients;
	private ContactsModel contactsModel;
	private String mGroupName;
	private AdView adView;

	/** Called when the activity is first created. */

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_list);
		ContactModelFactory.intializeContactModel(getApplication());
		contactsModel = ContactModelFactory.getContactModel();
		
		Intent intent = getIntent();
		if (intent != null) {
			savedRecipients = intent.getStringExtra("mrecipients");
			gatewayName = intent.getStringExtra("msgGateway");
			mGroupName = intent.getStringExtra("mgroupName");
		}

		EditText etxt_search = (EditText) findViewById(R.id.edittxt_contactsearch);
		listview_contact = (ListView) findViewById(R.id.lv_contactlist);
		listview_contact.setTextFilterEnabled(false);

		etxt_search.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence constrains, int start,
					int before, int count) {
				if (adapter == null) {
					loadRecipients();	
				}
				if (adapter.getFilter() == null) {
					return;
				}
				adapter.getFilter().filter(constrains);

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		Button button_finish = (Button) findViewById(R.id.button_finish);
		button_finish.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				for (int i = 0; i < list.size(); i++) {
					Model modelElement = list.get(i);
					if (modelElement.isSelected()) {
						recipients = recipients
								+ extractDisplayContact(modelElement.getName());
					}
				}
				Intent newsms;
				try {
					newsms = new Intent(getBaseContext(), Class
							.forName(gatewayName));
					Bundle bundle = new Bundle();
					bundle.putString("mrecipients", recipients);
					bundle.putString("mgroupName", mGroupName);
					newsms.putExtras(bundle);
					setResult(RESULT_OK, newsms);
					finish();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

		// Create the adView
		adView = new AdView(this, AdSize.BANNER, getResources().getString(
				R.string.AdID));
		LinearLayout layout = (LinearLayout) findViewById(R.id.llayout_contactslist);
		adView.setGravity(Gravity.TOP);
		layout.setGravity(Gravity.TOP);
		// Add the adView to it
		layout.addView(adView);
		adView.loadAd(new AdRequest());
	}

	@Override
	public void onDestroy() {
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		loadRecipients();
		if (adView != null) {
			adView.loadAd(new AdRequest());
		}
	}

	private void loadRecipients() {
		list = contactsModel.getContactList();
		if (savedRecipients != null && savedRecipients.length() > 0) {
			String[] recipentsArray = recipientsParser(savedRecipients);
			for (Model modelElement: list) {
				for (String temp : recipentsArray) 	{
					String modelName = extractDisplayContact(
							modelElement.getName()).replace(",", "");
					if (temp != null && temp.length() > 1
							&& modelName.equals(temp)) {
						modelElement.setSelected(true);
						break;
					}
					else{
						modelElement.setSelected(false);
					}
				}
			}
		}
		else{
			for(Model modelElement: list){
				modelElement.setSelected(false);
			}
		}
		adapter = new ContactArrayAdapter(this, R.layout.contact_list_tem, list);
		listview_contact.setAdapter(adapter);
	}

	private String extractDisplayContact(String name) {
		String mTemp = name;
		mTemp = mTemp.replaceAll("\\\n", "");
		String[] splitTemp = mTemp.split("\\\t");
		return (splitTemp[0] + ",");
	}

	private String[] recipientsParser(String recipients) {
		return recipients.split(",");
	}

	
}