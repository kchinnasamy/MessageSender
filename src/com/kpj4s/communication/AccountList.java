package com.kpj4s.communication;

import java.util.Locale;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.kpj4s.way2sms.R;

public class AccountList extends ListActivity {

	// Status of Account
	static int mAcCode = 3;

	// Status Of UserId
	static int mCode = 0;

	// Form an array specifying which columns to return.

	String[] mUsernames = null;

	// User's Account ID passed through intent
	String mSelecteduser;

	public static SharedPreferences myPrefs;

	DatabaseHelper dbhelper = new DatabaseHelper(this);

	final String TAG = "AccountLsit";

	private String gatewayName;

	Context baseContext;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		baseContext = getBaseContext();
		
		// load the appropriate Starting point
		Intent intent = getIntent();
		if (intent != null) {
			gatewayName = intent.getStringExtra("msgGateway");
		}

		myPrefs = this.getSharedPreferences(CommunicationConstants.MY_PREFS,
				MODE_PRIVATE);

		get_selectedUser_array();
		get_selectedUser();

	}

	private void get_selectedUser_array() {

		/** -----Saving the Contact names in the String[] Result ----- */
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		db.setLocale(Locale.getDefault());

		// Get a cursor over useraccount database.
		Cursor cur = db.query(DatabaseHelper.TABLE_USERACCOUNT, null, null,
				null, null, null, DatabaseHelper._ID);
		if (cur.getCount() == 0) {
			// No Accounts in Database so create a new one
			savePref(this, "No User", "No Password", "No Id");
			Intent newaccount = new Intent(this, NewAccount.class);
			Bundle bundle = new Bundle();
			bundle.putString("msgGateway", gatewayName);
			newaccount.putExtras(bundle);
			startActivity(newaccount);
			finish();
		} else {
			// Using the properties to get the index of the columns
			mUsernames = new String[cur.getCount()];
			int index = 0;
			while (cur.moveToNext()) {
				mUsernames[index] = cur.getString(1);
				index++;
			}
		}
		cur.close();
		db.close();

		/** ----Display the Contacts on the device----- */
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				mUsernames));

	}

	/** ----Defining the ItemOnClickListener for the displayed List---- */
	private void get_selectedUser() {

		final ListView mAccountlistview = getListView();
		mAccountlistview.setTextFilterEnabled(true);

		// Item ClickListener
		mAccountlistview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				// When clicked, show a toast with the TextView text
				Toast.makeText(
						baseContext,
						mAccountlistview.getItemAtPosition(position).toString(),
						Toast.LENGTH_SHORT).show();

				int index = 0;

				while (index <= mUsernames.length) {
					if (mAccountlistview.getItemAtPosition(position).toString()
							.equals(mUsernames[index])) {
						mSelecteduser = mUsernames[index];
						break;
					}
					index++;
				}

				SQLiteDatabase db = dbhelper.getWritableDatabase();
				db.setLocale(Locale.getDefault());

				// Get a cursor over useraccount database.
				Cursor cur = db.query(DatabaseHelper.TABLE_USERACCOUNT, null,
						null, null, null, null, DatabaseHelper._ID);
				cur.moveToPosition(position);
				savePref(baseContext, cur.getString(1), cur.getString(2),
						cur.getString(3));
				cur.close();
				db.close();
				/** -----Switching to the newSMS layout */
				Intent newsms;
				try {
					newsms = new Intent(baseContext,Class.forName(gatewayName));
					setResult(RESULT_OK, newsms);
					finish();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		});

		// Item LongClickListener
		mAccountlistview
				.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(final AdapterView<?> parent,
							View view, final int position, long id) {

						final String[] items = { "Change Password",
								"Delete Account" };

						AlertDialog.Builder builder = new AlertDialog.Builder(
								AccountList.this);
						builder.setTitle(mAccountlistview.getItemAtPosition(
								position).toString());
						builder.setItems(items,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int item) {
										switch (item) {

										// Change Password
										case 0:
											changePassword(position);
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
	}

	public void deleteAccount(int position) {
		DatabaseHelper dbhelper = new DatabaseHelper(AccountList.this);
		SQLiteDatabase db = dbhelper.getWritableDatabase();

		db.setLocale(Locale.getDefault());

		// Get a cursor over UserAccount database.
		Cursor cur = db.query(DatabaseHelper.TABLE_USERACCOUNT, null, null,
				null, null, null, DatabaseHelper._ID);
		cur.move(position);
		cur.moveToNext();
		db.delete(DatabaseHelper.TABLE_USERACCOUNT, "_id=?",
				new String[] { cur.getString(0) });
		cur.close();
		db.close();
	}

	public void changePassword(final int position) {

		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		final EditText input = new EditText(this);
		input.setInputType(0x00000081);// Making it a Password Field
		alert.setTitle("New Password");
		alert.setMessage(R.string.hint_chngpwd);
		alert.setView(input);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				String mPassword = input.getText().toString();

				DatabaseHelper dbhelper = new DatabaseHelper(AccountList.this);
				SQLiteDatabase db = dbhelper.getWritableDatabase();

				db.setLocale(Locale.getDefault());

				// Get a cursor over UserAccount database.
				Cursor cur = db.query(DatabaseHelper.TABLE_USERACCOUNT, null,
						null, null, null, null, DatabaseHelper._ID);
				cur.move(position);
				cur.moveToNext();
				ContentValues updatePassword = new ContentValues();
				updatePassword.put(DatabaseHelper.PASSWORD,
						mPassword);
				db.update(DatabaseHelper.TABLE_USERACCOUNT, updatePassword,
						"_id=?", new String[] { cur.getString(0) });

				Toast.makeText(baseContext, "Password Changed",
						Toast.LENGTH_SHORT).show();
				cur.close();
				db.close();

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

	private void confirmDelete(int pos) {
		final int position = pos;
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Confirm Delete");
		alert.setMessage(R.string.hint_delete);
		alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				deleteAccount(position);
				get_selectedUser_array();

				// Inform the User About the Action
				Toast.makeText(baseContext,
						"Account Deleted",
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
	
	// Write the prefix to the SharedPreferences object for this widget
	protected void savePref(Context context, String username, String password,
			String id) {
		SharedPreferences.Editor prefsEditor = myPrefs.edit();
		prefsEditor.putString(CommunicationConstants.PREFS_USER, username);
		prefsEditor.putString(CommunicationConstants.PREFS_PASSWORD, password);
		prefsEditor.putString(CommunicationConstants.PREFS_ID, id);
		prefsEditor.commit();

	}

}
