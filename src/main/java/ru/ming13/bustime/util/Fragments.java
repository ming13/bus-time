package ru.ming13.bustime.util;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public final class Fragments
{
	private Fragments() {
	}

	public static final class Arguments
	{
		private Arguments() {
		}

		public static final String ROUTE = "route";
		public static final String STOP = "stop";

		public static final String MESSAGE = "message";

		public static final String REQUEST_CODE = "request_code";
		public static final String ERROR_CODE = "error_code";
	}

	public static final class Operator
	{
		private final FragmentManager fragmentManager;

		public static Operator at(@NonNull FragmentActivity activity) {
			return new Operator(activity);
		}

		private Operator(FragmentActivity activity) {
			this.fragmentManager = activity.getSupportFragmentManager();
		}

		public Fragment get(@IdRes int fragmentContainerId) {
			return fragmentManager.findFragmentById(fragmentContainerId);
		}

		public void set(@NonNull Fragment fragment, @IdRes int fragmentContainerId) {
			if (!isSet(fragmentContainerId)) {
				fragmentManager
					.beginTransaction()
					.add(fragmentContainerId, fragment)
					.commit();
			}
		}

		private boolean isSet(@IdRes int fragmentContainerId) {
			return get(fragmentContainerId) != null;
		}

		public void resetFading(@NonNull Fragment fragment, @IdRes int fragmentContainerId) {
			fragmentManager
				.beginTransaction()
				.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
				.replace(fragmentContainerId, fragment)
				.commit();
		}

		public void resetSliding(@NonNull Fragment fragment, @IdRes int fragmentContainerId) {
			fragmentManager
				.beginTransaction()
				.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
				.replace(fragmentContainerId, fragment)
				.commit();
		}
	}
}
