package app.android.bustime.ui.dispatch.routes;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TimePicker;
import app.android.bustime.R;
import app.android.bustime.db.AlreadyExistsException;
import app.android.bustime.db.DbException;
import app.android.bustime.db.DbProvider;
import app.android.bustime.db.Route;
import app.android.bustime.db.Station;
import app.android.bustime.db.Time;
import app.android.bustime.ui.IntentFactory;
import app.android.bustime.ui.UserAlerter;


public class StationCreationActivity extends Activity
{
	private final Context activityContext = this;

	private Route route;

	private final static int LOCATION_REQUEST_CODE = 42;

	private final static double DEFAULT_LATITUDE = 55.534229;
	private final static double DEFAULT_LONGITUDE = 28.661546;

	private Station chosenExistingStation;
	private String stationName;
	private double stationLatitude = DEFAULT_LATITUDE;
	private double stationLongitude = DEFAULT_LONGITUDE;
	private int shiftTimeHour;
	private int shiftTimeMinute;

	private boolean isStationExist;

	private final List<HashMap<String, Object>> stationsData;
	private static final String SPINNER_ITEM_TEXT_ID = "text";
	private static final String SPINNER_ITEM_OBJECT_ID = "object";

	public StationCreationActivity() {
		super();

		stationsData = new ArrayList<HashMap<String, Object>>();
		isStationExist = false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_station_with_shift_time_creation);

		processReceivedRoute();

		initializeBodyControls();
	}

	private void processReceivedRoute() {
		Bundle receivedData = this.getIntent().getExtras();

		if (receivedData.containsKey(IntentFactory.MESSAGE_ID)) {
			route = receivedData.getParcelable(IntentFactory.MESSAGE_ID);
		} else {
			UserAlerter.alert(activityContext, getString(R.string.error_unspecified));

			finish();
		}
	}

	private void initializeBodyControls() {
		fillStationsSpinner();

		Button confirmButton = (Button) findViewById(R.id.confirm_button);
		confirmButton.setOnClickListener(confirmListener);

		TimePicker shiftTimePicker = (TimePicker) findViewById(R.id.shift_time_picker);
		shiftTimePicker.setIs24HourView(true);
		setUpNullTimeToShiftTimePicker();

		CheckBox stationWasCreatedCheckbox = (CheckBox) findViewById(R.id.checkbox_station_exists);
		stationWasCreatedCheckbox.setOnCheckedChangeListener(isStationExistListener);

		Button stationLocationButton = (Button) findViewById(R.id.button_station_location);
		stationLocationButton.setOnClickListener(stationLocationListener);
	}

	private void fillStationsSpinner() {
		new LoadStationsTask().execute();
	}

	private class LoadStationsTask extends AsyncTask<Void, Void, Void>
	{
		private List<Station> stationsList;

		@Override
		protected Void doInBackground(Void... params) {
			stationsList = DbProvider.getInstance().getStations().getStationsList();

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (stationsList.isEmpty()) {
				hidePossibilityToChooseExistingStation();
				return;
			}

			setUpStationsSpinner(stationsList);
		}
	}

	private void hidePossibilityToChooseExistingStation() {
		CheckBox stationWasCreateBox = (CheckBox) findViewById(R.id.checkbox_station_exists);
		stationWasCreateBox.setVisibility(View.GONE);
	}

	private void setUpStationsSpinner(List<Station> stationsList) {
		fillStationsData(stationsList);

		SimpleAdapter stationsAdapter = new SimpleAdapter(activityContext, stationsData,
			android.R.layout.simple_spinner_item, new String[]{SPINNER_ITEM_TEXT_ID},
			new int[]{android.R.id.text1});
		stationsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		Spinner stationsSpinner = (Spinner) findViewById(R.id.spinner_stations);
		stationsSpinner.setAdapter(stationsAdapter);
	}

	private void fillStationsData(List<Station> stationsList) {
		stationsData.clear();

		for (Station station : stationsList) {
			addItemToStationsData(station);
		}
	}

	private void addItemToStationsData(Station station) {
		HashMap<String, Object> stationItem = new HashMap<String, Object>();

		stationItem.put(SPINNER_ITEM_TEXT_ID, station.getName());
		stationItem.put(SPINNER_ITEM_OBJECT_ID, station);

		stationsData.add(stationItem);
	}

	private final OnClickListener confirmListener = new OnClickListener()
	{
		@Override
		public void onClick(View view) {
			readUserDataFromFields();

			String userDataErrorMessage = getUserDataErrorMessage();

			if (userDataErrorMessage.isEmpty()) {
				callStationCreation();
			} else {
				UserAlerter.alert(activityContext, userDataErrorMessage);
			}
		}

		private void callStationCreation() {
			new CreateStationTask().execute();
		}
	};

	private void readUserDataFromFields() {
		TimePicker shiftTimePicker = (TimePicker) findViewById(R.id.shift_time_picker);
		shiftTimeHour = shiftTimePicker.getCurrentHour();
		shiftTimeMinute = shiftTimePicker.getCurrentMinute();

		if (isStationExist) {
			chosenExistingStation = getChosenStation();
		} else {
			EditText stationNameEdit = (EditText) findViewById(R.id.edit_station_name);
			stationName = stationNameEdit.getText().toString().trim();
		}
	}

	private Station getChosenStation() {
		Spinner stationsSpinner = (Spinner) findViewById(R.id.spinner_stations);

		@SuppressWarnings(
			"unchecked") Map<String, Object> stationSpinnerItem = (Map<String, Object>) stationsSpinner.getSelectedItem();

		return (Station) stationSpinnerItem.get(SPINNER_ITEM_OBJECT_ID);
	}

	private String getUserDataErrorMessage() {
		if (isStationExist) {
			return new String();
		} else {
			return getStationNameErrorMessage();
		}
	}

	private String getStationNameErrorMessage() {
		if (stationName.isEmpty()) {
			return getString(R.string.error_empty_station_name);
		}

		return new String();
	}

	private class CreateStationTask extends AsyncTask<Void, Void, String>
	{
		@Override
		protected String doInBackground(Void... params) {

			Station stationToInsertShiftTime;

			if (isStationExist) {
				stationToInsertShiftTime = chosenExistingStation;
			} else {
				try {
					stationToInsertShiftTime = DbProvider.getInstance().getStations().createStation(
						stationName, stationLatitude, stationLongitude);
				}
				catch (AlreadyExistsException e) {
					return getString(R.string.error_station_exists);
				}
				catch (DbException e) {
					return getString(R.string.error_unspecified);
				}
			}

			try {
				stationToInsertShiftTime.insertShiftTimeForRoute(route,
					new Time(shiftTimeHour, shiftTimeMinute));
			}
			catch (AlreadyExistsException e) {
				if (!isStationExist) {
					DbProvider.getInstance().getStations().deleteStation(stationToInsertShiftTime);
				}

				return getString(R.string.error_shift_time_or_station_exist);
			}

			return new String();
		}

		@Override
		protected void onPostExecute(String errorMessage) {
			super.onPostExecute(errorMessage);

			if (errorMessage.isEmpty()) {
				finish();
			} else {
				UserAlerter.alert(activityContext, errorMessage);
			}
		}
	}

	private void setUpNullTimeToShiftTimePicker() {
		TimePicker shiftTimePicker = (TimePicker) findViewById(R.id.shift_time_picker);

		shiftTimePicker.setCurrentHour(0);
		shiftTimePicker.setCurrentMinute(0);
	}

	private final OnCheckedChangeListener isStationExistListener = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			isStationExist = isChecked;

			updateBodyControls();
		}
	};

	private void updateBodyControls() {
		EditText stationNameEdit = (EditText) findViewById(R.id.edit_station_name);
		Button stationLocationButton = (Button) findViewById(R.id.button_station_location);
		Spinner stationsListSpinner = (Spinner) findViewById(R.id.spinner_stations);

		if (isStationExist) {
			stationNameEdit.setVisibility(View.GONE);
			stationLocationButton.setVisibility(View.GONE);
			stationsListSpinner.setVisibility(View.VISIBLE);
		} else {
			stationNameEdit.setVisibility(View.VISIBLE);
			stationLocationButton.setVisibility(View.VISIBLE);
			stationsListSpinner.setVisibility(View.GONE);
		}
	}

	private final OnClickListener stationLocationListener = new OnClickListener()
	{
		@Override
		public void onClick(View view) {
			callStationLocationActivity();
		}

		private void callStationLocationActivity() {
			Intent callIntent = IntentFactory.createStationLocationIntent(activityContext,
				stationLatitude, stationLongitude);
			startActivityForResult(callIntent, LOCATION_REQUEST_CODE);
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if ((resultCode == RESULT_OK) && (requestCode == LOCATION_REQUEST_CODE)) {
			stationLatitude = data.getExtras().getDouble(IntentFactory.MESSAGE_ID);
			stationLongitude = data.getExtras().getDouble(IntentFactory.EXTRA_MESSAGE_ID);
		}
	}
}