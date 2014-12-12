package com.kpj4s.communication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static String DATABASE_NAME = "smslogin.db";
	public static String TABLE_USERACCOUNT = "useraccount";
	public static String TABLE_GROUPCONTACTS = "groupcontacts";
	public static String _ID = "_id";
	public static String USERNAME = "username";
	public static String PASSWORD = "password";
	public static String USERID = "userid";
	public static String GROUPNAME = "groupname";
	public static String CONTACTS = "contacts";
	
	String TAG = "Sms_Database";
	
	public DatabaseHelper(Context context) {
		super(context, DatabaseHelper.DATABASE_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS useraccount (_id INTEGER PRIMARY KEY AUTOINCREMENT, username STRING, password STRING, userid STRING);");
		db.execSQL("CREATE TABLE IF NOT EXISTS groupcontacts (_id INTEGER PRIMARY KEY AUTOINCREMENT, groupname STRING, contacts STRING);");
	}
	
	public void onOpen (SQLiteDatabase db){
		db.execSQL("CREATE TABLE IF NOT EXISTS useraccount (_id INTEGER PRIMARY KEY AUTOINCREMENT, username STRING, password STRING, userid STRING);");
		db.execSQL("CREATE TABLE IF NOT EXISTS groupcontacts (_id INTEGER PRIMARY KEY AUTOINCREMENT, groupname STRING, contacts STRING);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.v(TAG, "Upgrading database , which will destroy all the old data ");
		onCreate(db);
	}
}