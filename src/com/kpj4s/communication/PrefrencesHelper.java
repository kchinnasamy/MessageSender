package com.kpj4s.communication;

import android.content.SharedPreferences;

public class PrefrencesHelper {
	public static void setSendViaBackUp(SharedPreferences userPrefs,
			boolean isSendViaBackUp) {
		SharedPreferences.Editor prefsEditor = userPrefs.edit();
		prefsEditor.putBoolean(CommunicationConstants.PREFS_IS_SEND_VIA_BACKUP,
				isSendViaBackUp);
		prefsEditor.commit();
	}

	public static boolean shouldSendViaBackUp(SharedPreferences userPrefs) {
		return (userPrefs.getBoolean(
				CommunicationConstants.PREFS_IS_SEND_VIA_BACKUP, false));
	}
	
	public static long getLastJarUpdateTime(SharedPreferences userPrefs) {
		return (userPrefs.getLong(CommunicationConstants.PREFS_JAR_LAST_CHECKED, 0L));
	}
}
