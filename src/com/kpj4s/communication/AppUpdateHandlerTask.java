package com.kpj4s.communication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;

import com.kpj4s.way2sms.R;

public class AppUpdateHandlerTask extends AsyncTask<Void, Void, Integer> {

	private ProgressDialog dialog;
	private Activity activity;

	public AppUpdateHandlerTask(ProgressDialog dialog, Activity activity) {
		this.dialog = dialog;
		this.activity = activity;
		dialog.setMessage("Checking for Update...");

	}

	public void onPreExecute() {
		this.dialog.show();
	}

	@Override
	protected Integer doInBackground(Void... params) {
		// Checking for update
		String[] versions = getVersionInfo();
		if (versions == null || versions.length != 2) {
			return null;

		}
		Integer currentVersion = Integer.parseInt(versions[0]);

		return currentVersion;
	}

	public void onPostExecute(Integer currentVersion) {

		try {
			this.dialog.dismiss();
			dialog = null;
		} catch (Exception e) {
			e.printStackTrace();
		}

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

	}

	private String[] getVersionInfo() {
		URL url;
		try {
			url = new URL(activity.getResources().getString(
					R.string.version_url));
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String versionStr = reader.readLine();
			if (versionStr == null) {
				return null;
			}
			return versionStr.split(",");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

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
