package ru.ming13.bustime.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public final class Fragments
{
	private Fragments() {
	}

	public static final class Arguments
	{
		private Arguments() {
		}

		public static final String ERROR_CODE = "error_code";
		public static final String REQUEST_CODE = "request_code";
		public static final String URI = "uri";
	}

	public static final class Operator
	{
		private Operator() {
		}

		public static void add(FragmentActivity activity, Fragment fragment) {
			if (isAdded(activity)) {
				return;
			}

			activity.getSupportFragmentManager()
				.beginTransaction()
				.add(android.R.id.content, fragment)
				.commit();
		}

		private static boolean isAdded(FragmentActivity activity) {
			return activity.getSupportFragmentManager().findFragmentById(android.R.id.content) != null;
		}
	}
}
