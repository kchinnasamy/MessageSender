package com.kpj4s.communication;

import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.kpj4s.way2sms.R;

public class GroupContacts extends Activity {
	private static final int SELECTED = 0;

	final DatabaseHelper dbhelper = new DatabaseHelper(this);
	ListView lv_groupList;

	private String gatewayName;

	private AdView adView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_contacts);

		Intent intent = getIntent();
		if (intent != null) {
			gatewayName = intent.getStringExtra("msgGateway");
		}

		lv_groupList = (ListView) findViewById(R.id.lv_grouplist);

		lv_groupList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				// When clicked, show a toast with the TextView text
				Toast.makeText(getBaseContext(),
						lv_groupList.getItemAtPosition(position).toString(),
						Toast.LENGTH_SHORT).show();

				SQLiteDatabase db = dbhelper.getWritableDatabase();
				db.setLocale(Locale.getDefault());

				// Get a cursor over useraccount database.
				Cursor cur = db.query(DatabaseHelper.TABLE_GROUPCONTACTS, null, null, null, null,
						null, DatabaseHelper._ID);
				cur.moveToPosition(position);
				String groupContacts = cur.getString(2);
				cur.close();
				db.close();
				/** -----Switching to the newSMS layout */
				Intent newsms;
				try {
					newsms = new Intent(getBaseContext(), Class
							.forName(gatewayName));
					Bundle bundle = new Bundle();
					bundle.putString("mgroupContact", groupContacts);
					newsms.putExtras(bundle);
					setResult(RESULT_OK, newsms);
					finish();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		lv_groupList
				.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(final AdapterView<?> parent,
							View view, final int position, long id) {

						final String[] items = { "Edit Group", "Delete Group" };

						AlertDialog.Builder builder = new AlertDialog.Builder(
								GroupContacts.this);
						builder.setTitle(lv_groupList.getItemAtPosition(
								position).toString());
						builder.setItems(items,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int item) {
										switch (item) {

										// Change Password
										case 0:
											addEditGroup(position);
											break;

										// Delete Account
										case 1:
											confirmDelete(position);
											break;
										}
									}

								});

						AlertDialog alert = builder.create();
						alert.show();
						return true;
					}
				});

		Button button_addgroup = (Button) findViewById(R.id.button_addgroup);
		button_addgroup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addEditGroup(-1);
			}
		});

		// Create the adView
		adView = new AdView(this, AdSize.BANNER, getResources().getString(
				R.string.AdID));
		LinearLayout layout = (LinearLayout) findViewById(R.id.llayout_group_contacts);
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
		getGroupsList();
		if (adView != null) {
			adView.loadAd(new AdRequest());
		}
	}

	private void getGroupsList() {
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor cur = db.query(DatabaseHelper.TABLE_GROUPCONTACTS, null, null, null, null, null,
				DatabaseHelper._ID);
		String[] groupList = new String[cur.getCount()];
		int index = 0;
		while (cur.moveToNext()) {
			groupList[index] = cur.getString(1);
			System.out.println(groupList[index]);
			index++;
		}
		cur.close();
		db.close();
		lv_groupList.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, groupList));
	}

	public void deleteGroup(int position) {
		DatabaseHelper dbhelper = new DatabaseHelper(GroupContacts.this);
		SQLiteDatabase db = dbhelper.getWritableDatabase();

		db.setLocale(Locale.getDefault());

		// Get a cursor over UserAccount database.
		Cursor cur = db.query(DatabaseHelper.TABLE_GROUPCONTACTS, null, null, null, null, null,
				DatabaseHelper._ID);
		cur.move(position);
		cur.moveToNext();
		db.delete(DatabaseHelper.TABLE_GROUPCONTACTS, "_id=?", new String[] { cur.getString(0) });
		cur.close();
		db.close();
	}

	private void addEditGroup(int position) {
		Intent addEditGroup = new Intent(this, AddEditGroup.class);
		Bundle bundle = new Bundle();
		bundle.putInt("position", position);
		bundle.putString("msgGateway", this.getClass().getName());
		addEditGroup.putExtras(bundle);
		startActivity(addEditGroup);
	}

	private void confirmDelete(int pos) {
		final int position = pos;
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Confirm Delete");
		alert.setMessage(R.string.hint_delete);
		alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				deleteGroup(position);
				getGroupsList();

				// Inform the User About the Action
				Toast.makeText(getBaseContext(),
						R.string.toast_groupdeleted,
						Toast.LENGTH_SHORT).show();
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				});
		alert.show();
	}

	
	protected void launchContacts(String groupName) {
		Intent contacts = new Intent(this, ContactList.class);
		Bundle bundle = new Bundle();
		bundle.putString("mgroupName", groupName);
		bundle.putString("msgGateway", this.getClass().getName());
		contacts.putExtras(bundle);
		startActivityForResult(contacts, SELECTED);
	}
}