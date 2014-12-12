package com.kpj4s.communication;


public class CommunicationConstants {


	// Status of Account
	public static final int NOT_CREATED = 0;
	public static final int CREATED = 1;
	public static final int NOT_UNIQUE = 2;
	public static final int NO_USERID = 3;

	
	public static final int MS = 1; //Message Sent
	
	// Status Of UserId
	public static final int NOUID = 0;// UserId not Created
	public static final int UID = 1;// UserId Created
	public static final int LF = -4;// Login Failure
	public static final int CE = -3;// Connection Error
	public static final int BE = -2;// BackEnd Error
	public static final int IF = -1;// Internal Failure

	//Preference variable
	public static final String MY_PREFS = "myPrefs";
	public static final String GATEWAY_PREFS = "gateWayPrefs";
	public static final String PREFS_USER = "prefs_name";
	public static final String PREFS_PASSWORD = "prefs_password";
	public static final String PREFS_ID = "prefs_id";
	public static final String PREFS_DONT_SHOW_AGAIN = "prefs_dont_show_again";
	public static final String PREFS_LAST_SHOWN_APP_VERSION = "prefs_last_shown_app_version";
	public static final String PREFS_LAST_SHOWN_SPLASH_VERSION = "prefs_last_shown_notification_version";
	public static final String PREFS_AUTOCLEAR = "prefs_autoclear";
	public static final String PREFS_SMILEY = "prefs_smiley";
	public static final String PREFS_MGS_LAST_SENT = "prefs_msg_last_sent";
	public static final String PREFS_LAST_LOADED_JAR_VERSION = "prefs_last_loaded_jar_version";
	public static final String PREFS_IS_SEND_VIA_BACKUP = "prefs_is_send_via_backup";
	public static final String PREFS_JAR_LAST_CHECKED = "prefs_jar_last_checked";
	//Database Variables
	
	// SMS Size
	public final static int MSG_SIZE = 140;
	public final static int MSG_COUNT = 0;
	
	//GatewayIDs
	public static final int WAY2SMS_GATEWAY_ID = 1; 
	public static final int ONESIXTYBYTWO_GATEWAY_ID = 2;
	
	//Resousrce
	public static final String R_STRING = "string";
	public static final String R_LAYOUT = "layout";
	public static final String R_ID = "id";

	//TaskName
	public static final int INITIALIZE_TASK = 0;
	public static final int SEND_TASK = 1; 
	
	public static final int UPDATE_DELAY_TIME = 900000;
	

}
