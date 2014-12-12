package com.kpj4s.communication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;

import com.kpj4s.way2sms.R;

public class SplashScreen extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splashscreen);
		
		final CheckBox check_dontshow = (CheckBox) findViewById(R.id.checkbox_donotshowagain);

		Intent intent = getIntent();
		String splashContent = null;
		if (intent != null) {
			splashContent = intent.getStringExtra("splashContent");
			boolean displayDontShowAgainOption = intent.getBooleanExtra(
					"displayDontShowAgainOption", false);
			check_dontshow
					.setVisibility(displayDontShowAgainOption ? View.VISIBLE
							: View.INVISIBLE);
		}

		WebView webview_splash = (WebView) findViewById(R.id.websplash);
		String mime = "text/html";
		String encoding = "utf-8";
		webview_splash.getSettings().setJavaScriptEnabled(true);
		if (splashContent != null)
			webview_splash.loadDataWithBaseURL(null, splashContent, mime,
					encoding, null);

		Button button_dismiss = (Button) findViewById(R.id.button_dismiss);
		button_dismiss.setOnClickListener(new OnClickListener() {

			SharedPreferences userPrefs = getSharedPreferences(
					CommunicationConstants.MY_PREFS, Context.MODE_PRIVATE);

			@Override
			public void onClick(View v) {
				if (check_dontshow.getVisibility() == View.VISIBLE
						&& check_dontshow.isChecked()) {
					userPrefs
							.edit()
							.putBoolean(
									CommunicationConstants.PREFS_DONT_SHOW_AGAIN,
									true).commit();
				}
				finish();
			}
		});

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
}
