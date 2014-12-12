package com.kpj4s.communication;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.kpj4s.way2sms.R;

public class AddEditGroup extends Activity {

	private static final int SELECTED = 0;
	private EditText etxt_groupName;
	private Button button_groupContats;
	private Button button_groupSave;
	private MultiAutoCompleteTextView mactxtview_contactList;
	private int position;
	private DatabaseHelper dbhelper = new DatabaseHelper(this);
	private ArrayAdapter<Model> mAdapter;
	private ContactsModel contactsModel;
	private AdView adView;

	/** Called when the activity is first created. */

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_group_contact);

		ContactModelFactory.intializeContactModel(getApplication());
		contactsModel = ContactModelFactory.getContactModel();

		Intent intent = getIntent();
		if (intent != null) {
			position = intent.getIntExtra("position", -1);
		}

		etxt_groupName = (EditText) findViewById(R.id.etxt_groupname);
		mactxtview_contactList = (MultiAutoCompleteTextView) findViewById(R.id.mactxtview_contactlist);
		button_groupContats = (Button) findViewById(R.id.button_groupcontact);
		button_groupSave = (Button) findViewById(R.id.button_groupsave);

		mAdapter = new ArrayAdapter<Model>(this,
				R.layout.smart_contact_list_item, new ArrayList<Model>());
		new ContactsObserver(getApplication(), mAdapter, contactsModel)
				.onChange(true);
		mactxtview_contactList = (MultiAutoCompleteTextView) findViewById(R.id.mactxtview_contactlist);
		mactxtview_contactList.setAdapter(mAdapter);

		new ContactsHandler(mactxtview_contactList, button_groupContats, this);

		// Loading the data into the fields
		if (position != -1)
			getGroupContact(position);

		// button_groupSave
		button_groupSave.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				addEditGroupContact(position);

			}
		});

		// Create the adView
		adView = new AdView(this, AdSize.BANNER, getResources().getString(
				R.string.AdID));
		LinearLayout layout = (LinearLayout) findViewById(R.id.llayout_edit_group);
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
		if (adView != null) {
			adView.loadAd(new AdRequest());
		}
	}

	private void getGroupContact(int position) {
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor cur = db.query(DatabaseHelper.TABLE_GROUPCONTACTS, null, null,
				null, null, null, DatabaseHelper._ID);
		cur.moveToPosition(position);
		etxt_groupName.setText(cur.getString(1));
		mactxtview_contactList.setText(cur.getString(2));
		cur.close();
		db.close();
	}

	private void addEditGroupContact(int position) {
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		ContentValues cv = new ContentValues();

		String recipients = mactxtview_contactList.getText().toString();
		String mGroupName = etxt_groupName.getText().toString();

		if (recipients != null && recipients.length() > 0 && mGroupName != null
				&& mGroupName.length() > 0) {
			if (position != -1) {

				Cursor cur = db.query(DatabaseHelper.TABLE_GROUPCONTACTS, null,
						null, null, null, null, DatabaseHelper._ID);
				cur.moveToPosition(position);

				cv.put(DatabaseHelper.GROUPNAME, mGroupName);
				cv.put(DatabaseHelper.CONTACTS, recipients);

				String where = "groupname=?";
				String value[] = new String[] { cur.getString(1) };

				// Update the database
				db.update(DatabaseHelper.TABLE_GROUPCONTACTS, cv, where, value);

				Toast.makeText(getBaseContext(), R.string.toast_changesaved,
						Toast.LENGTH_LONG).show();

				cur.close();

				finish();

			} else if (position == -1) {
				boolean mUnique = false;
				Cursor cur = db.query(DatabaseHelper.TABLE_GROUPCONTACTS, null,
						null, null, null, null, DatabaseHelper._ID);

				while (cur.moveToNext()) {
					if (cur.getString(1).equalsIgnoreCase(mGroupName)) {
						mUnique = true;
						break;
					}
				}

				cur.close();

				// Check for uniqueness of the group
				if (mUnique) {
					Toast.makeText(getBaseContext(),
							R.string.toast_groupexists, Toast.LENGTH_LONG)
							.show();
				} else {

					db.setLocale(Locale.getDefault());
					cv.put(DatabaseHelper.GROUPNAME, mGroupName);
					cv.put(DatabaseHelper.CONTACTS, recipients);
					db.insert(DatabaseHelper.TABLE_GROUPCONTACTS,
							DatabaseHelper.CONTACTS, cv);
					Toast.makeText(getBaseContext(), R.string.toast_groupcreated,
							Toast.LENGTH_LONG).show();

					finish();
				}

			}

		} else {
			Toast.makeText(getBaseContext(), R.string.toast_emptyFeild,
					Toast.LENGTH_LONG).show();
		}
		db.close();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (data != null)
			switch (requestCode) {
			case SELECTED: {
				Bundle extras = data.getExtras();

				if (extras != null) {
					// Contact Selected
					if (extras.containsKey("mrecipients")) {
						String recipients = extras.getString("mrecipients");
						if (recipients != null && recipients.length() > 0) {
							mactxtview_contactList.setText(recipients);
							mactxtview_contactList
									.setSelection(mactxtview_contactList
											.length());
						}
					}
				}
			}
			}
	}
}
