package com.kpj4s.communication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersionHelper {
	public static String[] getVersionInfo() {
		URL url;
		try {
			url = new URL(
					"https://dl.dropbox.com/u/22979833/way2sms-version-23.txt");
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
		}
		return null;
	}
}
