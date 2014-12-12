package com.kpj4s.communication;

import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.kpj4s.way2sms.R;

public class NewAccount extends Activity {
	public static SharedPreferences myPrefs;
	static int mCode = 0;
	static int mAcCode = 3;

	DatabaseHelper dbhelper = new DatabaseHelper(this);

	static ProgressDialog dialog;

	String TAG = "NewAccount";

	EditText etxt_username;
	EditText etxt_password;

	Button button_save;
	Button button_done;

	String mUsername;
	String mPassword;
	// Maximum digits for Phone Number
	int mDigits = 10;

	static Context baseContext = null;

	private AdView adView = null;

	private String gatewayName;

	private Activity activity;
	private ProgressDialog dialogUpdate;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_account);
		baseContext = getBaseContext();

		activity = this;

		// load the appropriate Starting point
		Intent intent = getIntent();
		if (intent != null) {
			gatewayName = intent.getStringExtra("msgGateway");
		}

		TextView txt_loginhint = (TextView) findViewById(R.id.txtview_loginhint);
		txt_loginhint.setMovementMethod(LinkMovementMethod.getInstance());

		myPrefs = this.getSharedPreferences(CommunicationConstants.MY_PREFS,
				MODE_PRIVATE);

		etxt_password = (EditText) findViewById(R.id.etxt_password);
		etxt_username = (EditText) findViewById(R.id.etxt_username);

		dialog = new ProgressDialog(this);
		dialog.setMessage("Verifying and Saving Credentials...");
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);

		dialogUpdate = new ProgressDialog(this);
		dialogUpdate.setIndeterminate(true);
		dialogUpdate.setCancelable(false);

		// Setting The Maximum Character Length for etxt_msg
		InputFilter[] charlimit = new InputFilter[1];
		charlimit[0] = new InputFilter.LengthFilter(mDigits);
		etxt_username.setFilters(charlimit);

		button_save = (Button) findViewById(R.id.button_save);
		button_done = (Button) findViewById(R.id.button_done);

		SQLiteDatabase db = dbhelper.getReadableDatabase();

		// Get a cursor over userAccount database.
		Cursor cur = db.query(DatabaseHelper.TABLE_USERACCOUNT, null, null,
				null, null, null, DatabaseHelper._ID);
		if (cur.getCount() == 0) {
			button_done.setEnabled(false);
		}
		cur.close();
		db.close();

		// Button Done
		button_done.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent newsms;
				try {
					newsms = new Intent(baseContext, Class.forName(gatewayName));
					startActivity(newsms);
					finish();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

		// Button Save
		button_save.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				mUsername = etxt_username.getText().toString();
				mPassword = etxt_password.getText().toString();
				if (mUsername.length() == 10 && mPassword.length() > 0)
					saveCredentials();
				else {
					etxt_username.setText(null);
					etxt_password.setText(null);
					Toast.makeText(baseContext, R.string.toast_loginFailure,
							Toast.LENGTH_LONG).show();
				}
			}
		});

		// Create the adView
		adView = new AdView(this, AdSize.BANNER, getResources().getString(
				R.string.AdID));
		LinearLayout layout = (LinearLayout) findViewById(R.id.llayout_new_account);
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

	// Verify and Saving the Credentials
	private void saveCredentials() {
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		db.setLocale(Locale.getDefault());

		// Get a cursor over userAccount database.
		Cursor cur = db.query(DatabaseHelper.TABLE_USERACCOUNT, null, null,
				null, null, null, DatabaseHelper._ID);

		/** Checking for Empty fields of UserName and Password */

		if (mUsername.length() == 10 && mPassword.length() > 0) {

			switch (saveAccount(mUsername, mPassword)) {
			case CommunicationConstants.CREATED: {

				if (cur.getCount() == 1) {
					cur.moveToFirst();
					savePref(baseContext, cur.getString(1), cur.getString(2),
							cur.getString(3));
				}
				mAcCode = CommunicationConstants.CREATED;
				break;
			}
			case CommunicationConstants.NOT_CREATED: {
				mAcCode = CommunicationConstants.NOT_CREATED;
				break;
			}
			case CommunicationConstants.NOT_UNIQUE: {
				mAcCode = CommunicationConstants.NOT_UNIQUE;
				break;
			}
			case CommunicationConstants.NO_USERID: {
				mAcCode = CommunicationConstants.NO_USERID;
				break;
			}
			default:
				break;
			}
		} else {
			mAcCode = CommunicationConstants.NO_USERID;
			mCode = CommunicationConstants.LF;
		}
		cur.close();
		db.close();

		displayStatus();
	}

	// Saving the Credentials in database
	protected int saveAccount(String userName, String password) {
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		boolean mNotUnique = true;
		ContentValues cv = new ContentValues();

		db.setLocale(Locale.getDefault());

		Cursor cur = db.query(DatabaseHelper.TABLE_USERACCOUNT, null, null,
				null, null, null, DatabaseHelper._ID);
		while (cur.moveToNext()) {
			if (cur.getString(1).equals(userName)) {
				mNotUnique = false;
				break;
			}
		}
		if (mNotUnique) {
			cv.put(DatabaseHelper.USERNAME, userName);
			cv.put(DatabaseHelper.PASSWORD, password);
			db.insert(DatabaseHelper.TABLE_USERACCOUNT, password, cv);
			return CommunicationConstants.CREATED;
		} else if (!mNotUnique) {
			return CommunicationConstants.NOT_UNIQUE;
		}
		cur.close();
		db.close();
		return CommunicationConstants.NO_USERID;
	}

	protected void savePref(Context context, String username, String password,
			String id) {
		SharedPreferences.Editor prefsEditor = myPrefs.edit();
		prefsEditor.putString(CommunicationConstants.PREFS_USER, username);
		prefsEditor.putString(CommunicationConstants.PREFS_PASSWORD, password);
		prefsEditor.putString(CommunicationConstants.PREFS_ID, id);
		prefsEditor.commit();

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// preventing default implementation previous to
			// android.os.Build.VERSION_CODES.ECLAIR
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void displayStatus() {
		// UserID Avail and Account Status
		switch (mAcCode) {
		case CommunicationConstants.CREATED: {
			Toast.makeText(baseContext, R.string.toast_accountCreated,
					Toast.LENGTH_LONG).show();
			button_done.setEnabled(true);
			etxt_username.setText(null);
			etxt_password.setText(null);
			break;
		}
		case CommunicationConstants.NOT_UNIQUE: {
			Toast.makeText(baseContext, R.string.toast_accountExits,
					Toast.LENGTH_LONG).show();
			etxt_username.setText(null);
			etxt_password.setText(null);
			break;
		}

		case CommunicationConstants.NO_USERID:
			break;

		}
		// User ID Status
		switch (mCode) {

		case CommunicationConstants.UID:
			break;
		case CommunicationConstants.NOUID:
			break;
		case CommunicationConstants.LF: {
			etxt_password.setText(null);
			Toast.makeText(baseContext, R.string.toast_loginFailure,
					Toast.LENGTH_LONG).show();
			break;
		}
		case CommunicationConstants.BE: {
			Toast.makeText(baseContext, R.string.toast_backendError,
					Toast.LENGTH_LONG).show();
			break;
		}
		case CommunicationConstants.CE: {
			Toast.makeText(baseContext, R.string.toast_connectionError,
					Toast.LENGTH_LONG).show();
			break;
		}
		case CommunicationConstants.IF: {
			new AppUpdateHandlerTask(dialogUpdate, activity).execute();
			Toast.makeText(baseContext, R.string.toast_internalFailure,
					Toast.LENGTH_LONG).show();
			break;
		}
		}
	}
}