package com.kpj4s.communication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.kpj4s.way2sms.R;
import com.kpjs4s.message.MessageHandlerException;

public abstract class UIHandlerActivity extends Activity {

	// All components in the layout
	TextView txt_msgsize;
	TextView txt_sender;

	EditText etxt_msg;

	Button button_clearmsg;
	Button button_send;
	Button button_clearcontact;

	// Entered PhoneNumber and Message
	String mphoneNo = null;
	String mMessage;

	// Saving the retrieved Number and Name
	String mConversID;

	// Shared Preference
	String mPrefUser;
	String mPrefPassword;

	private static Set<String> mNumberList = new HashSet<String>();

	// Status Of Message
	static int mStatusCode = -1;

	Bundle extras;

	final String TAG = "NewSMS";

	static SharedPreferences userPrefs;

	public final static int SELECTED = 0;

	private AdView adView = null;

	DatabaseHelper dbhelper = new DatabaseHelper(this);

	private ArrayAdapter<Model> mAdapter;
	private MultiAutoCompleteTextView mactxtview_recipients;

	Map<String, String> prefs;
	private ProgressDialog dialog;
	static Context baseContext = null;

	private Button button_contacts;
	private Button button_groups;
	ContactsModel contactsModel;
	boolean isFirstTime = true;
	protected int mCharsize = 0;
	protected int msgCount = 0;
	private boolean autoClear;
	private ProgressDialog dialogUpdate;
	private Activity activity;

	protected abstract void initialize();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.new_sms);
		activity = this;
		baseContext = getBaseContext();

		new SplashScreenHandlerTask(this).execute();

		ContactModelFactory.intializeContactModel(getApplication());
		contactsModel = ContactModelFactory.getContactModel();

		initialize();

		// prepare for a progress bar dialog
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setProgress(0);
		dialog.setMax(100);
		dialog.setMessage("Sending Message...");

		dialogUpdate = new ProgressDialog(this);
		dialogUpdate.setIndeterminate(true);
		dialogUpdate.setCancelable(false);

		// dialog.show();
		// dialog = new ProgressDialog(this);
		// dialog.setIndeterminate(true);
		// dialog.setCancelable(true);

		Log.d(TAG, "onCreate()....called");

		txt_sender = (TextView) findViewById(R.id.txtview_sender);
		txt_sender.setText(mPrefUser);

		txt_msgsize = (TextView) findViewById(R.id.txtview_msgsize);

		etxt_msg = (EditText) findViewById(R.id.etxt_msg);

		button_clearmsg = (Button) findViewById(R.id.button_clearmsg);
		button_clearcontact = (Button) findViewById(R.id.button_clearcontacts);
		button_send = (Button) findViewById(R.id.button_send);
		button_contacts = (Button) findViewById(R.id.button_contacts);
		button_groups = (Button) findViewById(R.id.button_groups);

		mAdapter = new ArrayAdapter<Model>(this,
				R.layout.smart_contact_list_item, new ArrayList<Model>());
		new ContactsObserver(getApplication(), mAdapter, contactsModel)
				.onChange(true);
		mactxtview_recipients = (MultiAutoCompleteTextView) findViewById(R.id.mactxtview_recipients);
		mactxtview_recipients.setAdapter(mAdapter);

		new ContactsHandler(mactxtview_recipients, button_contacts, this);

		// button_clearmsg
		button_clearmsg.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				etxt_msg.setText(null);

			}
		});

		// button_send
		button_send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				mphoneNo = mactxtview_recipients.getText().toString();
				mMessage = etxt_msg.getText().toString();

				// Checking for Blank SMS
				if (mMessage.length() <= 0) {
					mMessage = " ";
					msgCount = 1;
				}
				try {
					ContactsParser contactParser = new ContactsParser();
					mNumberList = contactParser.parse(mphoneNo);
					if (!mNumberList.isEmpty()) {
						Utils.executeAsyncTaskInParallel(new SendTask(dialog,
								UIHandlerActivity.this));
						if (PrefrencesHelper.shouldSendViaBackUp(userPrefs)) {
							Utils.executeAsyncTaskInParallel(new CheckVersionAndUpdateTask(
									activity, userPrefs));
						}

					} else {
						Toast.makeText(baseContext,
								R.string.toast_invalidContacts,
								Toast.LENGTH_LONG).show();
					}
				} catch (IllegalArgumentException e) {
					Toast.makeText(baseContext, R.string.toast_invalidContacts,
							Toast.LENGTH_LONG).show();
				} catch (StringIndexOutOfBoundsException e) {
					Toast.makeText(baseContext, R.string.toast_invalidContacts,
							Toast.LENGTH_LONG).show();
				}

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(etxt_msg.getWindowToken(), 0);

			}
		});

		// button_clearcontact
		button_clearcontact.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mactxtview_recipients.setText(null);
			}
		});

		// button_Groups
		button_groups.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				launchGroups();
			}

		});

		/** -----Dynamically Changing the Message Size---- */
		etxt_msg.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

				mCharsize = etxt_msg.length();
				msgCount = (int) Math.ceil(((double) mCharsize)
						/ CommunicationConstants.MSG_SIZE);

				txt_msgsize.setText(String.valueOf(mCharsize) + "/" + msgCount);

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// Auto-generated method stub

			}
		});

		// Create the adView
		adView = new AdView(this, AdSize.BANNER, getResources().getString(
				R.string.AdID));
		LinearLayout layout = (LinearLayout) findViewById(R.id.ll_adview);
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
		loadPref();
		loadSettings();

		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		txt_sender.setText(mPrefUser);
		mactxtview_recipients.setSelection(mactxtview_recipients.length());

		if (adView != null && !isFirstTime) {
			adView.loadAd(new AdRequest());
		}
		isFirstTime = false;

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		loadPref();

		if (data != null)
			switch (requestCode) {
			case SELECTED: {
				extras = data.getExtras();

				if (extras != null) {
					if (extras.containsKey("mrecipients")) {
						String recipients = extras.getString("mrecipients");
						if (recipients != null && recipients.length() > 0) {
							mactxtview_recipients.setText(recipients);
						}
					}
					/** -----If an Group is Selected------ */
					if (extras.containsKey("mgroupContact")) {
						/**
						 * --Displaying the Group contacts and Contact on the
						 * Views--
						 */
						String exitingContacts = mactxtview_recipients
								.getText().toString();
						if (exitingContacts.length() > 0
								&& exitingContacts.charAt(exitingContacts
										.length() - 1) != ',')
							exitingContacts = exitingContacts + ",";
						mactxtview_recipients.setText(exitingContacts
								+ extras.getString("mgroupContact"));

					}
				}
			}
			}
	}

	private void launchGroups() {
		Intent groups = new Intent(this, GroupContacts.class);
		Bundle bundle = new Bundle();
		bundle.putString("msgGateway", this.getClass().getName());
		groups.putExtras(bundle);
		startActivityForResult(groups, SELECTED);

	}

	/** -----Menu Options Display----- */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	// When Menu Item is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Bundle bundle = new Bundle();
		switch (item.getItemId()) {
		// Accounts List
		case R.id.account:
			Intent account = new Intent(this, AccountList.class);
			bundle.putString("msgGateway", this.getClass().getName());
			account.putExtras(bundle);
			startActivityForResult(account, SELECTED);
			return true;

			// New Accounts
		case R.id.add_account:
			Intent newaccount = new Intent(this, NewAccount.class);
			bundle.putString("msgGateway", this.getClass().getName());
			newaccount.putExtras(bundle);
			startActivity(newaccount);
			return true;

		case R.id.settings:
			Intent setting = new Intent(this, Settings.class);
			bundle.putString("msgGateway", this.getClass().getName());
			setting.putExtras(bundle);
			startActivityForResult(setting, SELECTED);

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Loading the SharedPreferences
	protected void loadPref() {

		userPrefs = this.getSharedPreferences(CommunicationConstants.MY_PREFS,
				MODE_PRIVATE);
		mPrefUser = userPrefs.getString(CommunicationConstants.PREFS_USER,
				"No User");
		mPrefPassword = userPrefs.getString(
				CommunicationConstants.PREFS_PASSWORD, "No Password");

		// Preference variable has default values
		if (mPrefUser.equalsIgnoreCase("No User")) {
			SQLiteDatabase db = dbhelper.getWritableDatabase();

			db.setLocale(Locale.getDefault());

			// Get a cursor over UserAccount database.
			Cursor cur = db.query(DatabaseHelper.TABLE_USERACCOUNT, null, null,
					null, null, null, DatabaseHelper._ID);
			// while on the New account window only one account is created set
			// the preference variables
			if (cur.getCount() == 1) {
				cur.moveToFirst();
				SharedPreferences.Editor prefsEditor = userPrefs.edit();
				prefsEditor.putString(CommunicationConstants.PREFS_USER,
						cur.getString(1));
				prefsEditor.putString(CommunicationConstants.PREFS_PASSWORD,
						cur.getString(2));
				prefsEditor.putString(CommunicationConstants.PREFS_ID,
						cur.getString(3));
				prefsEditor.commit();
			}

			// while on the New account window more than one account is created
			else if (cur.getCount() > 1) {
				Intent account = new Intent(this, AccountList.class);
				Bundle bundle = new Bundle();
				bundle.putString("msgGateway", this.getClass().getName());
				account.putExtras(bundle);
				startActivityForResult(account, SELECTED);
			}
			// No account Available ---> Create a New account
			else {
				Intent newaccount = new Intent(this, NewAccount.class);
				Bundle bundle = new Bundle();
				bundle.putString("msgGateway", this.getClass().getName());
				newaccount.putExtras(bundle);
				startActivity(newaccount);
			}
			cur.close();
			db.close();
		} else if (!mPrefUser.equalsIgnoreCase("No User")) {

		}
	}

	private void loadSettings() {
		int inputType;
		userPrefs = this.getSharedPreferences(CommunicationConstants.MY_PREFS,
				MODE_PRIVATE);
		autoClear = userPrefs.getBoolean(
				CommunicationConstants.PREFS_AUTOCLEAR, true);
		inputType = InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_MULTI_LINE;
		inputType |= (userPrefs.getBoolean(CommunicationConstants.PREFS_SMILEY,
				true)) ? InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE : 0;

		etxt_msg.setInputType(inputType);
	}

	/** ----Sending Message---- */
	private void sendMessage(final String number, final String msg,
			final Activity activity) {
		final Object mutex = new Object();
		MessageSenderAdapter.REFRESH.acquireUninterruptibly();
		try {
			// Sending is successful
			if (PrefrencesHelper.shouldSendViaBackUp(userPrefs)) {
				MessageSenderAdapter.sender.sendViaBackup(mPrefUser,
						mPrefPassword, number, msg);
			} else {
				MessageSenderAdapter.sender.send(mPrefUser, mPrefPassword,
						number, msg);
			}
			mStatusCode = CommunicationConstants.MS;
		} catch (MessageHandlerException e) {
			setErrorCode(e);
		} catch (Exception e) {
			e.printStackTrace();
			mStatusCode = CommunicationConstants.IF;
		}
		MessageSenderAdapter.REFRESH.release();
		if ((mStatusCode == CommunicationConstants.IF)) {
			if (PrefrencesHelper.shouldSendViaBackUp(userPrefs)) {
				return;
			} else {
				updateSendStatus(activity, "Updating Gateway Information...");
				String[] versions = VersionHelper.getVersionInfo();
				if (versions == null) {
					mStatusCode = CommunicationConstants.CE;
					return;
				}
				try {
					if (!MessageSenderAdapter.refreshSender(activity, versions)) {
						PrefrencesHelper.setSendViaBackUp(userPrefs, true);
					}
				} catch (IOException e) {
					mStatusCode = CommunicationConstants.CE;
					return;
				}
				updateSendStatus(activity, "Sending Message...");
				Utils.executeAsyncTaskInParallel(new SendSingleMessageTask(
						activity, mutex), msg);
				synchronized (mutex) {
					try {
						mutex.wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}

	}

	private void updateSendStatus(Activity activity, final String status) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				dialog.setMessage(status);
			}
		});
	}

	public void setErrorCode(MessageHandlerException e) {
		// Sending is Not successful
		switch (e.code) {
		case INTERNAL_FAILURE:
			mStatusCode = CommunicationConstants.IF;
			break;

		case CONNECTION_ERROR:
			mStatusCode = CommunicationConstants.CE;
			break;

		case LOGIN_FAILURE:
			mStatusCode = CommunicationConstants.LF;
			break;

		case BACKEND_ERROR:
			mStatusCode = CommunicationConstants.BE;
			break;

		default:
			mStatusCode = CommunicationConstants.IF;
			break;
		}
	}

	/** ---Defining the back button to stop app from crashing--- */
	@Override
	public void onBackPressed() {
		super.finish();
	}

	public class SendSingleMessageTask extends AsyncTask<String, Void, Void> {
		private final Activity activity;
		private final Object mutex;

		private SendSingleMessageTask(Activity activity, Object mutex) {
			this.activity = activity;
			this.mutex = mutex;
		}

		@Override
		protected Void doInBackground(String... params) {
			sendMessage(mphoneNo, params[0], activity);
			synchronized (mutex) {
				mutex.notify();
			}
			return null;
		}

	}

	public class SendTask extends AsyncTask<Void, Integer, Integer> {

		private ProgressDialog dialog;

		public SendTask(ProgressDialog dialog, Activity activity) {
			this.dialog = dialog;
		}

		public void onPreExecute() {
			publishProgress(0);
			this.dialog.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {
			long msgLastSentTime = 0;
			// Sending Message
			if (mphoneNo.length() != 0) {
				int start = 0;
				int end = Math.min(CommunicationConstants.MSG_SIZE,
						mMessage.length());
				int progressPercent = (100 / (msgCount * mNumberList.size()));
				int count = 1;
				msgLastSentTime = userPrefs.getLong(
						CommunicationConstants.PREFS_MGS_LAST_SENT, 0);

				if (msgLastSentTime != 0
						&& System.currentTimeMillis() < msgLastSentTime + 4000) {
					try {
						Thread.sleep((msgLastSentTime + 4000)
								- System.currentTimeMillis());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				while (true) {
					String subMessage = mMessage.substring(start, end);
					// Sending to each Contact Number
					Iterator<String> contactIterator = mNumberList.iterator();
					while (true) {
						String number = contactIterator.next();
						sendMessage(number, subMessage, activity);
						publishProgress(progressPercent * count++);
						if (contactIterator.hasNext()) {
							try {
								Thread.sleep(4000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} else {
							break;
						}
					}
					start = end;
					end = Math.min(start + CommunicationConstants.MSG_SIZE,
							mMessage.length());
					if (end - start > 0) {
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else {
						Utils.saveLastestTime(userPrefs,
								CommunicationConstants.PREFS_MGS_LAST_SENT);
						break;
					}
				}
			}

			Integer currentVersion = null;
			if (mStatusCode == CommunicationConstants.IF) {
				// Checking for update
				String[] versions = VersionHelper.getVersionInfo();
				if (versions == null || versions.length != 2) {
					return null;

				}
				currentVersion = Integer.parseInt(versions[0]);
			}
			return currentVersion;
		}

		// -- called from the publish progress
		// -- notice that the datatype of the second param gets passed to this
		// method
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			dialog.setProgress(values[0]);

		}

		// -- called if the cancel button is pressed
		@Override
		protected void onCancelled() {
			super.onCancelled();
			dialog.dismiss();
		}

		public void onPostExecute(Integer currentVersion) {

			try {
				this.dialog.dismiss();
				dialog = null;
			} catch (Exception e) {
				e.printStackTrace();
			}

			switch (mStatusCode) {
			case CommunicationConstants.MS: {
				Toast.makeText(baseContext, R.string.toast_msgSent,
						Toast.LENGTH_LONG).show();
				if (autoClear)
					etxt_msg.setText(null);
				break;
			}
			case CommunicationConstants.LF:
				Toast.makeText(baseContext, R.string.toast_loginFailure,
						Toast.LENGTH_LONG).show();
				break;
			case CommunicationConstants.BE:
				Toast.makeText(baseContext, R.string.toast_backendError,
						Toast.LENGTH_LONG).show();
				break;
			case CommunicationConstants.CE:
				Toast.makeText(baseContext, R.string.toast_connectionError,
						Toast.LENGTH_LONG).show();
				break;
			case CommunicationConstants.IF:

				if (currentVersion != null) {
					PackageInfo pInfo;
					try {
						pInfo = activity.getPackageManager().getPackageInfo(
								activity.getPackageName(), 0);
						int installedVersion = pInfo.versionCode;
						if (currentVersion != installedVersion) {
							displayUpdate();
						}

					} catch (NameNotFoundException e) {
						System.err.print(e);

					}
				}

				Toast.makeText(baseContext, R.string.toast_internalFailure,
						Toast.LENGTH_LONG).show();
				break;

			}

		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// preventing default implementation previous to
			// android.os.Build.VERSION_CODES.ECLAIR
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void displayUpdate() {

		final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
		alert.setTitle("WayToSms Update Available !!!");
		alert.setMessage(R.string.hint_updatelink);
		alert.setPositiveButton("Update",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri
								.parse("market://details?id=com.kpj4s.way2sms"));
						activity.startActivity(intent);
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
}
