package com.kpj4s.communication;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;

public class Utils {
	@SuppressLint({ "NewApi" })
	public static <Params, Progress, Result> void executeAsyncTaskInParallel(
			AsyncTask<Params, Progress, Result> task, Params... params) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		} else {
			task.execute(params);
		}
	}

	public static void saveLastestTime(SharedPreferences userPrefs,
			String prefrenceName) {
		SharedPreferences.Editor prefsEditor = userPrefs.edit();
		prefsEditor.putLong(prefrenceName, System.currentTimeMillis());
		prefsEditor.commit();
	}
}
