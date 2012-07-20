package app.android.bustime.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import app.android.bustime.db.Route;
import app.android.bustime.db.Station;


final class FragmentFactory
{
	private static final String MESSAGE_ID;
	private static final String EXTRA_MESSAGE_ID;

	static {
		MESSAGE_ID = String.format("%s.message", FragmentFactory.class.getPackage().getName());
		EXTRA_MESSAGE_ID = String.format("%s.extramessage", IntentFactory.class.getPackage().getName());
	}

	private FragmentFactory() {
	}

	public static String getMessageId() {
		return MESSAGE_ID;
	}

	public static String getExtraMessageId() {
		return EXTRA_MESSAGE_ID;
	}

	public static Fragment createRoutesFragment(Context context) {
		return Fragment.instantiate(context, RoutesFragment.class.getName(), new Bundle());
	}

	public static Fragment createRoutesFragment(Context context, Station station) {
		Bundle arguments = new Bundle();
		arguments.putParcelable(MESSAGE_ID, station);

		return Fragment.instantiate(context, RoutesFragment.class.getName(), arguments);
	}

	public static Fragment createStationsFragment(Context context) {
		return Fragment.instantiate(context, StationsFragment.class.getName(), new Bundle());
	}

	public static Fragment createStationsFragment(Context context, Route route) {
		Bundle arguments = new Bundle();
		arguments.putParcelable(MESSAGE_ID, route);

		return Fragment.instantiate(context, StationsFragment.class.getName(), arguments);
	}

	public static Fragment createFullWeekTimetableFragment(Context context, Route route, Station station) {
		Bundle arguments = new Bundle();
		arguments.putParcelable(MESSAGE_ID, route);
		arguments.putParcelable(EXTRA_MESSAGE_ID, station);

		return Fragment.instantiate(context, FullWeekTimetableFragment.class.getName(), arguments);
	}

	public static Fragment createWorkdaysTimetableFragment(Context context, Route route, Station station) {
		Bundle arguments = new Bundle();
		arguments.putParcelable(MESSAGE_ID, route);
		arguments.putParcelable(EXTRA_MESSAGE_ID, station);

		return Fragment.instantiate(context, WorkdaysTimetableFragment.class.getName(), arguments);
	}

	public static Fragment createWeekendTimetableFragment(Context context, Route route, Station station) {
		Bundle arguments = new Bundle();
		arguments.putParcelable(MESSAGE_ID, route);
		arguments.putParcelable(EXTRA_MESSAGE_ID, station);

		return Fragment.instantiate(context, WeekendTimetableFragment.class.getName(), arguments);
	}
}
