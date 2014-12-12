package com.kpj4s.way2sms;

import java.io.IOException;

import android.widget.Toast;

import com.kpj4s.communication.MessageSenderAdapter;
import com.kpj4s.communication.UIHandlerActivity;

public class Way2Sms extends UIHandlerActivity {
	@Override
	protected void initialize() {
		try {
			MessageSenderAdapter.initialize(this);
		} catch (IOException e) {
			Toast.makeText(this,
					"An Expection has occured. Contact kpjsfours@gmail.com",
					Toast.LENGTH_LONG).show();
		}
	}
}
