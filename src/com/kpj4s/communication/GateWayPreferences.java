//package com.kpj4s.communication;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//
//import com.kpjs4s.message.ParametersRepository;
//
//public class GateWayPreferences implements ParametersRepository{
//	static Context context;
//	static SharedPreferences mgateWayPreferences;
//	
//	public GateWayPreferences(Context context){
//		GateWayPreferences.context = context;
//		mgateWayPreferences = context.getSharedPreferences(
//				CommunicationConstants.GATEWAY_PREFS,Context.MODE_PRIVATE);
//	}
//
//		@Override
//	public String get(String key) {
//		String prefsValue = mgateWayPreferences.getString(key,
//				"null");
//		if(!prefsValue.equals("null")){
//			return prefsValue;
//		}
//		return null;
//	}
//
//	@Override
//	public void put(String key, String value) {
//		SharedPreferences.Editor prefsEditor = mgateWayPreferences.edit();
//		prefsEditor.putString(key, value);
//		prefsEditor.commit();
//	}
//}
