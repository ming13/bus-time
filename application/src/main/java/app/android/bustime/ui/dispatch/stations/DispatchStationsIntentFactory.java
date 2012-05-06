package app.android.bustime.ui.dispatch.stations;


import android.content.Context;
import android.content.Intent;
import app.android.bustime.local.Station;
import app.android.bustime.ui.IntentFactory;


class DispatchStationsIntentFactory extends IntentFactory
{
	public static Intent createStationCreationIntent(Context context) {
		Intent intent = new Intent(context, StationCreationActivity.class);

		return intent;
	}

	public static Intent createRoutesListIntent(Context context, Station station) {
		Intent intent = new Intent(context, RoutesListActivity.class);
		intent.putExtra(MESSAGE_ID, station);

		return intent;
	}
}
