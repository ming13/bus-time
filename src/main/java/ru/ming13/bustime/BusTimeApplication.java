package ru.ming13.bustime;

import android.app.Application;
import android.os.StrictMode;

public class BusTimeApplication extends Application
{
	@Override
	public void onCreate() {
		super.onCreate();

		if (isDebugging()) {
			setUpDetecting();
		}
	}

	private boolean isDebugging() {
		return BuildConfig.DEBUG;
	}

	private void setUpDetecting() {
		StrictMode.enableDefaults();
	}
}
