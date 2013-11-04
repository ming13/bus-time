package ru.ming13.bustime.util;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import ru.ming13.bustime.fragment.GooglePlayServicesErrorDialog;

public final class MapsUtil
{
	private static final int REQUEST_CODE = 0;

	private final Context context;

	public static MapsUtil with(Context context) {
		return new MapsUtil(context);
	}

	private MapsUtil(Context context) {
		this.context = context;
	}

	public boolean areMapsAvailable() {
		return getErrorCode() == ConnectionResult.SUCCESS;
	}

	private int getErrorCode() {
		return GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
	}

	public void showErrorDialog(FragmentManager fragmentManager) {
		showErrorDialog(fragmentManager, getErrorCode());
	}

	private void showErrorDialog(FragmentManager fragmentManager, int errorCode) {
		DialogFragment dialog = GooglePlayServicesErrorDialog.newInstance(errorCode, REQUEST_CODE);
		dialog.show(fragmentManager, GooglePlayServicesErrorDialog.TAG);
	}

	public boolean isResolvable(ConnectionResult connectionResult) {
		return connectionResult.hasResolution();
	}

	public void resolveError(ConnectionResult connectionResult) {
		try {
			connectionResult.startResolutionForResult((Activity) context, REQUEST_CODE);
		} catch (IntentSender.SendIntentException e) {
			throw new RuntimeException();
		}
	}

	public void showErrorDialog(FragmentManager fragmentManager, ConnectionResult connectionResult) {
		showErrorDialog(fragmentManager, connectionResult.getErrorCode());
	}
}
