package com.kpj4s.communication;

import java.io.IOException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;

public class CheckVersionAndUpdateTask extends AsyncTask<Void, Void, Void> {
	Activity activity;
	SharedPreferences userPrefs;

	CheckVersionAndUpdateTask(Activity activity, SharedPreferences userPrefs) {
		this.activity = activity;
		this.userPrefs = userPrefs;
	}

	@Override
	protected Void doInBackground(Void... params) {
		String versionInfo[] = null;
		try {
			long lastChecked = PrefrencesHelper.getLastJarUpdateTime(userPrefs);
			if(lastChecked != 0 && lastChecked > lastChecked + CommunicationConstants.UPDATE_DELAY_TIME){
				return null;
			}
			Utils.saveLastestTime(userPrefs,
					CommunicationConstants.PREFS_JAR_LAST_CHECKED);
			versionInfo = VersionHelper.getVersionInfo();
			MessageSenderAdapter.refreshSender(activity, versionInfo);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}