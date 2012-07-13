package app.android.bustime.ui.dispatch.routes;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import app.android.bustime.R;
import app.android.bustime.db.DbProvider;
import app.android.bustime.db.Route;
import app.android.bustime.db.Station;
import app.android.bustime.ui.IntentFactory;
import app.android.bustime.ui.SimpleAdapterListActivity;
import app.android.bustime.ui.UserAlerter;


public class StationsListActivity extends SimpleAdapterListActivity
{
	private final Context activityContext = this;

	private static final String LIST_ITEM_TEXT_ID = "text";
	private static final String LIST_ITEM_OBJECT_ID = "object";

	private Route route;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stations);

		processReceivedRoute();

		initializeList();
	}

	private void processReceivedRoute() {
		Bundle receivedData = this.getIntent().getExtras();

		if (receivedData.containsKey(IntentFactory.MESSAGE_ID)) {
			route = receivedData.getParcelable(IntentFactory.MESSAGE_ID);
		}
		else {
			UserAlerter.alert(activityContext, getString(R.string.error_unspecified));

			finish();
		}
	}

	@Override
	protected void initializeList() {
		SimpleAdapter stationsAdapter = new SimpleAdapter(activityContext, listData,
			R.layout.list_item_one_line, new String[] {LIST_ITEM_TEXT_ID}, new int[] {R.id.text});

		setListAdapter(stationsAdapter);

		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		registerForContextMenu(getListView());
	}

	@Override
	protected void onResume() {
		super.onResume();

		loadStations();
	}

	private void loadStations() {
		new LoadStationsTask().execute();
	}

	private class LoadStationsTask extends AsyncTask<Void, Void, Void>
	{
		private List<Station> stations;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			setEmptyListText(getString(R.string.loading_stations));
		}

		@Override
		protected Void doInBackground(Void... params) {
			stations = DbProvider.getInstance().getStations().getStationsList(route);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (stations.isEmpty()) {
				setEmptyListText(getString(R.string.empty_stations));
			}
			else {
				fillList(stations);
			}
		}
	}

	@Override
	protected void addItemToList(Object itemData) {
		Station station = (Station) itemData;

		HashMap<String, Object> stationItem = new HashMap<String, Object>();

		stationItem.put(LIST_ITEM_TEXT_ID, station.getName());
		stationItem.put(LIST_ITEM_OBJECT_ID, station);

		listData.add(stationItem);
	}

	private Station getStation(int stationPosition) {
		SimpleAdapter listAdapter = (SimpleAdapter) getListAdapter();

		@SuppressWarnings(
			"unchecked") Map<String, Object> adapterItem = (Map<String, Object>) listAdapter.getItem(
			stationPosition);

		return (Station) adapterItem.get(LIST_ITEM_OBJECT_ID);
	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);

		callTimetable(position);
	}

	private void callTimetable(int stationPosition) {
		Station station = getStation(stationPosition);

		Intent callIntent = IntentFactory.createTimetableIntent(activityContext, route, station);
		startActivity(callIntent);
	}
}
