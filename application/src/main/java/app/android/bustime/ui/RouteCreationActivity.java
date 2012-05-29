package app.android.bustime.ui;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import app.android.bustime.R;
import app.android.bustime.db.AlreadyExistsException;
import app.android.bustime.db.DbException;
import app.android.bustime.db.DbProvider;
import app.android.bustime.db.Route;


public class RouteCreationActivity extends FormActivity
{
	private String routeName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_route_creation);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected Button getConfirmButton() {
		return (Button) findViewById(R.id.button_confirm);
	}

	@Override
	protected void readUserDataFromFields() {
		EditText routeNameEdit = (EditText) findViewById(R.id.edit_route_name);

		routeName = routeNameEdit.getText().toString().trim();
	}

	@Override
	protected String getUserDataErrorMessage() {
		if (routeName.isEmpty()) {
			return getString(R.string.error_empty_route_name);
		}

		return new String();
	}

	@Override
	protected void performSubmitAction() {
		new CreateRouteTask().execute();
	}

	private class CreateRouteTask extends AsyncTask<Void, Void, String>
	{
		private Route route;

		@Override
		protected String doInBackground(Void... params) {
			try {
				route = DbProvider.getInstance().getRoutes().createRoute(routeName);
			}
			catch (AlreadyExistsException e) {
				return getString(R.string.error_route_exists);
			}
			catch (DbException e) {
				return getString(R.string.error_unspecified);
			}

			return new String();
		}

		@Override
		protected void onPostExecute(String errorMessage) {
			super.onPostExecute(errorMessage);

			if (errorMessage.isEmpty()) {
				callDepartureTimetable();

				finish();
			} else {
				UserAlerter.alert(activityContext, errorMessage);
			}
		}

		private void callDepartureTimetable() {
			Intent callIntent = IntentFactory.createDepartureTimetableIntent(activityContext, route);
			startActivity(callIntent);
		}
	}
}
