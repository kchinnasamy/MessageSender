package com.kpj4s.communication;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.CheckBox;

import com.kpj4s.way2sms.R;

public class Settings extends Activity {

	public static SharedPreferences myPrefs;

	private CheckBox checkbox_autoclear;
	private CheckBox checkbox_simley;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		
		checkbox_autoclear = (CheckBox) findViewById(R.id.checkbox_autoclear);
		checkbox_simley = (CheckBox) findViewById(R.id.checkbox_simley);

		myPrefs = this.getSharedPreferences(CommunicationConstants.MY_PREFS,
				MODE_PRIVATE);
		
		checkbox_autoclear.setChecked(myPrefs.getBoolean(
				CommunicationConstants.PREFS_AUTOCLEAR, true));

		checkbox_simley.setChecked(myPrefs.getBoolean(
				CommunicationConstants.PREFS_SMILEY, true));

	}

	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// preventing default implementation previous to
			// android.os.Build.VERSION_CODES.ECLAIR
			// load the appropriate Starting point

			SharedPreferences.Editor prefsEditor = myPrefs.edit();
			prefsEditor.putBoolean(CommunicationConstants.PREFS_AUTOCLEAR,
					checkbox_autoclear.isChecked());
			prefsEditor.putBoolean(CommunicationConstants.PREFS_SMILEY,
					checkbox_simley.isChecked());
			prefsEditor.commit();

			finish();
			
		}
		return super.onKeyDown(keyCode, event);
	}

}