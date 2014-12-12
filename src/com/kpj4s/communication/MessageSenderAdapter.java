package com.kpj4s.communication;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.Semaphore;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.kpjs4s.message.MessageSender;
import com.kpjs4s.message.MessageSenderFactory;

import dalvik.system.DexClassLoader;

public class MessageSenderAdapter {
	public static final Semaphore REFRESH = new Semaphore(1, true);
	public static MessageSender sender;
	private static ProgressDialog dialog;
	private static final String MESSAGE_SENDER_JAR = "MessageSenderImpl.jar";

	// Buffer size for file copying. While 8kb is used in this sample, you
	// may want to tweak it based on actual size of the secondary dex file
	// involved.
	private static final int BUF_SIZE = 8 * 1024;

	public static boolean refreshSender(Activity activity, String[] versionInfo)
			throws IOException {
		SharedPreferences userPrefs = activity.getSharedPreferences(
				CommunicationConstants.MY_PREFS, Context.MODE_PRIVATE);
		int lastLoadedJarVersion = userPrefs.getInt(
				CommunicationConstants.PREFS_LAST_LOADED_JAR_VERSION, -1);
		if (versionInfo == null
				|| (versionInfo.length == 3 && Integer.parseInt(versionInfo[2]) == lastLoadedJarVersion)) {
			return false;
		}

		final File dexInternalStoragePath = new File(activity.getDir("dex",
				Context.MODE_PRIVATE), MESSAGE_SENDER_JAR);
		URL url = new URL(
				"https://dl.dropbox.com/u/22979833/MessageSenderImpl.jar");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		Utils.executeAsyncTaskInParallel(new FetchAndLoadSenderFactoryTask(
				null, activity, new BufferedInputStream(conn.getInputStream()),
				Integer.parseInt(versionInfo[2])), dexInternalStoragePath);
		return true;

	}

	public static void initialize(Activity activity) throws IOException {
		// prepare for a progress bar dialog
		dialog = new ProgressDialog(activity);
		dialog.setCancelable(false);
		dialog.setMessage("Updating App data. Please wait...");
		if (sender != null) {
			return;
		}
		File optimizedDexOutputPath = new File(activity.getFilesDir()
				+ File.separator + "outdex");

		if (optimizedDexOutputPath != null
				&& optimizedDexOutputPath.isDirectory()) {
			deleteDirectory(optimizedDexOutputPath);
		}
		final File dexInternalStoragePath = new File(activity.getDir("dex",
				Context.MODE_PRIVATE), MESSAGE_SENDER_JAR);
		if (dexInternalStoragePath.exists()) {
			FetchAndLoadSenderFactoryTask.loadMessageSenderFactory(activity);
			return;
		}
		BufferedInputStream jarStream = new BufferedInputStream(activity
				.getAssets().open(MESSAGE_SENDER_JAR));

		Utils.executeAsyncTaskInParallel(new FetchAndLoadSenderFactoryTask(
				dialog, activity, jarStream, 1), dexInternalStoragePath);

	}

	private static void deleteDirectory(File directory) {
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				deleteDirectory(file);
			} else {
				file.delete();
			}
		}
		directory.delete();
	}

	public static class FetchAndLoadSenderFactoryTask extends
			AsyncTask<File, Void, Boolean> {

		private Activity activity;
		private ProgressDialog mProgressDialog;
		private BufferedInputStream jarStream;
		private int jarVersion;

		public FetchAndLoadSenderFactoryTask(ProgressDialog mProgressDialog,
				Activity activity, BufferedInputStream jarStream, int jarVersion) {
			this.activity = activity;
			this.mProgressDialog = mProgressDialog;
			this.jarVersion = jarVersion;
			this.jarStream = jarStream;

		}

		@Override
		protected void onPreExecute() {
			REFRESH.acquireUninterruptibly();
			if (mProgressDialog != null) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mProgressDialog.show();
					}
				});
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			if (mProgressDialog != null)
				mProgressDialog.cancel();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (mProgressDialog != null)
				mProgressDialog.cancel();
			if (result) {
				loadMessageSenderFactory(activity);
				SharedPreferences userPrefs = activity.getSharedPreferences(
						CommunicationConstants.MY_PREFS, Context.MODE_PRIVATE);
				saveLoadedJarVersion(userPrefs, jarVersion);
				PrefrencesHelper.setSendViaBackUp(userPrefs, false);
			}
			REFRESH.release();
		}

		@Override
		protected Boolean doInBackground(File... dexInternalStoragePaths) {
			synchronized (REFRESH) {
				return prepareDex(dexInternalStoragePaths[0]);
			}
		}

		// File I/O code to copy the secondary dex file from asset resource to
		// internal storage.
		private boolean prepareDex(File dexInternalStoragePath) {
			OutputStream dexWriter = null;

			try {

				dexWriter = new BufferedOutputStream(new FileOutputStream(
						dexInternalStoragePath));
				byte[] buf = new byte[BUF_SIZE];
				int len;
				while ((len = jarStream.read(buf, 0, BUF_SIZE)) > 0) {
					dexWriter.write(buf, 0, len);
				}
				dexWriter.close();
				jarStream.close();
				return true;
			} catch (IOException e) {
				if (dexWriter != null) {
					try {
						dexWriter.close();
					} catch (IOException ioe) {
						System.out.println(ioe);
					}
				}
				if (jarStream != null) {
					try {
						jarStream.close();
					} catch (IOException ioe) {
						System.out.println(ioe);
					}
				}
				return false;

			}
		}

		public static void loadMessageSenderFactory(Context context) {
			// Internal storage where the DexClassLoader writes the optimised
			// dex file to.
			File optimizedDexOutputPath = new File(context.getFilesDir()
					+ File.separator + "outdex" + File.separator + "outdex"
					+ new Random().nextDouble());
			optimizedDexOutputPath.mkdirs();

			final File dexInternalStoragePath = new File(context.getDir("dex",
					Context.MODE_PRIVATE), MESSAGE_SENDER_JAR);
			// Initialise the class loader with the secondary dex file.
			DexClassLoader cl = new DexClassLoader(
					dexInternalStoragePath.getAbsolutePath(),
					optimizedDexOutputPath.getAbsolutePath(), null,
					context.getClassLoader());
			Class<?> factoryImplClass = null;

			try {
				// Load the library class from the class loader.
				factoryImplClass = cl
						.loadClass("com.kpjs4s.message.MessageSenderFactoryImpl");

				// Cast the return object to the library interface so that the
				// caller can directly invoke methods in the interface.
				// Alternatively, the caller can invoke methods through
				// reflection,
				// which is more verbose and slow.
				MessageSenderFactory messageSenderFactory = (MessageSenderFactory) factoryImplClass
						.newInstance();
				MessageSenderAdapter.sender = messageSenderFactory
						.getWay2SmsMessageSender();

			} catch (Exception exception) {
				// Handle exception gracefully here.
				exception.printStackTrace();
			}
		}
	}

	private static void saveLoadedJarVersion(SharedPreferences userPrefs,
			int version) {
		SharedPreferences.Editor prefsEditor = userPrefs.edit();
		prefsEditor.putInt(
				CommunicationConstants.PREFS_LAST_LOADED_JAR_VERSION, version);
		prefsEditor.commit();
	}
}
