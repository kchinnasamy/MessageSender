package com.kpj4s.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;

import com.kpj4s.way2sms.R;

public class SplashScreenHandlerTask extends AsyncTask<Void, Void, Void> {
	private Activity activity;

	public SplashScreenHandlerTask(Activity activity) {
		this.activity = activity;
	}

	@Override
	protected Void doInBackground(Void... params) {
		String[] versions = VersionHelper.getVersionInfo();
		if (versions == null || versions.length != 3) {
			return null;
		}
		int currentVersion = Integer.parseInt(versions[0]);

		PackageInfo pInfo;
		try {
			pInfo = activity.getPackageManager().getPackageInfo(
					activity.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			return null;
		}
		int installedVersion = pInfo.versionCode;

		String splashUrl = null;
		boolean displayDontShowAgainOption = false;
		if (currentVersion != installedVersion) {
			splashUrl = activity.getResources().getString(
					R.string.default_splash_url);
			displayDontShowAgainOption = false;
		} else {
			try {
				MessageSenderAdapter.refreshSender(activity, versions);
			} catch (IOException e) {
			}
			SharedPreferences userPrefs = activity.getSharedPreferences(
					CommunicationConstants.MY_PREFS, Context.MODE_PRIVATE);
			int lastShownAppVersion = userPrefs.getInt(
					CommunicationConstants.PREFS_LAST_SHOWN_APP_VERSION, -1);
			int lastShownSplashVersion = userPrefs.getInt(
					CommunicationConstants.PREFS_LAST_SHOWN_SPLASH_VERSION, -1);
			// check
			boolean dontShowAgain = userPrefs.getBoolean(
					CommunicationConstants.PREFS_DONT_SHOW_AGAIN, false);
			int currentSplashVersion = Integer.parseInt(versions[1]);
			if (lastShownAppVersion == -1
					|| lastShownSplashVersion != currentSplashVersion
					|| !dontShowAgain) {
				splashUrl = activity.getResources().getString(
						R.string.current_splash_url);
				userPrefs
						.edit()
						.putInt(CommunicationConstants.PREFS_LAST_SHOWN_APP_VERSION,
								currentVersion)
						.putInt(CommunicationConstants.PREFS_LAST_SHOWN_SPLASH_VERSION,
								currentSplashVersion)
						.putBoolean(
								CommunicationConstants.PREFS_DONT_SHOW_AGAIN,
								false).commit();
				displayDontShowAgainOption = true;
			}
		}
		if (splashUrl != null) {
			String splashContent = readSplashContent(splashUrl);
			if (splashContent == null || splashContent.length() <= 0) {
				return null;
			}
			Intent splashscreen = new Intent(activity, SplashScreen.class);
			Bundle bundle = new Bundle();
			bundle.putString("splashContent", splashContent);
			bundle.putBoolean("displayDontShowAgainOption",
					displayDontShowAgainOption);
			splashscreen.putExtras(bundle);
			activity.startActivity(splashscreen);
		}
		return null;
	}

	private String readSplashContent(String splashUrl) {
		URL url;
		try {
			url = new URL(splashUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			StringBuilder content = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line);
			}
			return content.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}