package ru.ming13.bustime.ui.activity;


import java.util.HashMap;
import java.util.Map;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import ru.ming13.bustime.R;
import ru.ming13.bustime.ui.fragment.IntermediateProgressDialog;
import ru.ming13.bustime.ui.fragment.RoutesFragment;
import ru.ming13.bustime.ui.fragment.StationsFragment;
import ru.ming13.bustime.ui.intent.IntentFactory;
import ru.ming13.bustime.ui.task.DatabaseUpdateCheckTask;
import ru.ming13.bustime.ui.task.DatabaseUpdateTask;
import ru.ming13.bustime.ui.util.UserAlerter;


public class HomeActivity extends SherlockFragmentActivity implements DatabaseUpdateCheckTask.DatabaseUpdateCheckCallback, DatabaseUpdateTask.DatabaseUpdateCallback
{
	private static final class LastInstanceKeys
	{
		private LastInstanceKeys() {
		}

		public static final String SELECTED_TAB = "selected_tab";

		public static final String DATABASE_UPDATE_CHECK_TASK = "update_check_task";
		public static final String DATABASE_UPDATE_TASK = "update_task";
	}

	private DatabaseUpdateCheckTask databaseUpdateCheckTask;
	private DatabaseUpdateTask databaseUpdateTask;

	private RoutesFragment routesFragment;
	private StationsFragment stationsFragment;

	private boolean isPaused;

	private boolean isUpdatingDialogHidingOnResumeRequested;

	private boolean isUpdatingAvailable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setUpTabs();

		restoreLastInstanceState();

		callDatabaseUpdateCheck();
	}

	private void setUpTabs() {
		ActionBar actionBar = getSupportActionBar();

		actionBar.addTab(buildRoutesTab());
		actionBar.addTab(buildStationsTab());
	}

	private ActionBar.Tab buildRoutesTab() {
		ActionBar.Tab tab = getSupportActionBar().newTab();

		routesFragment = RoutesFragment.newInstance();

		tab.setText(R.string.title_routes);
		tab.setTabListener(new TabListener(routesFragment));

		return tab;
	}

	public static class TabListener implements ActionBar.TabListener
	{
		private final Fragment fragment;

		public TabListener(Fragment fragment) {
			this.fragment = fragment;
		}

		@Override
		public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
			if (fragment.isDetached()) {
				fragmentTransaction.attach(fragment);
			}
			else {
				fragmentTransaction.replace(android.R.id.content, fragment);
			}
		}

		@Override
		public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
			if (!fragment.isDetached()) {
				fragmentTransaction.detach(fragment);
			}
		}

		@Override
		public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		}
	}

	private ActionBar.Tab buildStationsTab() {
		ActionBar.Tab tab = getSupportActionBar().newTab();

		stationsFragment = StationsFragment.newAllLoadingInstance();

		tab.setText(R.string.title_stations);
		tab.setTabListener(new TabListener(stationsFragment));

		return tab;
	}

	private void restoreLastInstanceState() {
		if (!isLastInstanceValid()) {
			return;
		}

		restoreLastInstanceSelectedTab();

		restoreLastInstanceDatabaseUpdateCheckTask();
		restoreLastInstanceDatabaseUpdateTask();
	}

	private boolean isLastInstanceValid() {
		return getLastCustomNonConfigurationInstance() != null;
	}

	private void restoreLastInstanceSelectedTab() {
		if (!isLastInstanceElementValid(LastInstanceKeys.SELECTED_TAB)) {
			return;
		}

		setSelectedTab((Integer) getLastInstanceElement(LastInstanceKeys.SELECTED_TAB));
	}

	private boolean isLastInstanceElementValid(String lastInstanceKey) {
		Map<String, Object> lastInstance = getLastInstance();

		if (!lastInstance.containsKey(lastInstanceKey)) {
			return false;
		}

		return lastInstance.get(lastInstanceKey) != null;
	}

	private Map<String, Object> getLastInstance() {
		return (Map<String, Object>) getLastCustomNonConfigurationInstance();
	}

	private <LastInstanceElementType> LastInstanceElementType getLastInstanceElement(String lastInstanceKey) {
		Map<String, Object> lastInstance = getLastInstance();

		return (LastInstanceElementType) lastInstance.get(lastInstanceKey);
	}

	private void setSelectedTab(int tabPosition) {
		getSupportActionBar().setSelectedNavigationItem(tabPosition);
	}

	private void restoreLastInstanceDatabaseUpdateCheckTask() {
		if (!isLastInstanceElementValid(LastInstanceKeys.DATABASE_UPDATE_CHECK_TASK)) {
			return;
		}

		databaseUpdateCheckTask = getLastInstanceElement(LastInstanceKeys.DATABASE_UPDATE_CHECK_TASK);

		databaseUpdateCheckTask.setContext(this);
		databaseUpdateCheckTask.setDatabaseUpdateCheckCallback(this);
	}

	private void restoreLastInstanceDatabaseUpdateTask() {
		if (!isLastInstanceElementValid(LastInstanceKeys.DATABASE_UPDATE_TASK)) {
			return;
		}

		databaseUpdateTask = getLastInstanceElement(LastInstanceKeys.DATABASE_UPDATE_TASK);

		databaseUpdateTask.setContext(this);
		databaseUpdateTask.setDatabaseUpdateCallback(this);
	}

	private void callDatabaseUpdateCheck() {
		if (databaseUpdateCheckTask != null) {
			return;
		}

		isUpdatingAvailable = false;

		databaseUpdateCheckTask = DatabaseUpdateCheckTask.newInstance(this, this);
		databaseUpdateCheckTask.execute();
	}

	@Override
	public void onAvailableUpdate() {
		isUpdatingAvailable = true;

		showUpdatingAvailableMessage();

		updateActionBarButtons();
	}

	private void showUpdatingAvailableMessage() {
		getSupportActionBar().setSubtitle(R.string.warning_update_available);
	}

	private void updateActionBarButtons() {
		getSherlock().dispatchInvalidateOptionsMenu();
	}

	@Override
	public void onFailedUpdateCheck() {
	}

	@Override
	public void onNoUpdatesEver() {
		callDatabaseUpdate();
	}

	private void callDatabaseUpdate() {
		showUpdatingProgressDialog();

		databaseUpdateTask = DatabaseUpdateTask.newInstance(this, this);
		databaseUpdateTask.execute();
	}

	private void showUpdatingProgressDialog() {
		IntermediateProgressDialog progressDialog = IntermediateProgressDialog.newInstance(
			getString(R.string.loading_update));

		progressDialog.show(getSupportFragmentManager(), IntermediateProgressDialog.TAG);
	}

	@Override
	public void onSuccessUpdate() {
		isUpdatingAvailable = false;

		hideUpdatingAvailableMessage();

		refreshFragments();

		hideUpdatingProgressDialog();

		updateActionBarButtons();
	}

	private void hideUpdatingAvailableMessage() {
		getSupportActionBar().setSubtitle(null);
	}

	private void refreshFragments() {
		if (routesFragment.isAdded()) {
			routesFragment.callListRepopulation();
		}

		if (stationsFragment.isAdded()) {
			stationsFragment.callListRepopulation();
		}
	}

	private void hideUpdatingProgressDialog() {
		if (isPaused()) {
			setUpdatingDialogHidingOnResumeRequested(true);

			return;
		}

		IntermediateProgressDialog progressDialog = (IntermediateProgressDialog) getSupportFragmentManager().findFragmentByTag(
			IntermediateProgressDialog.TAG);

		if (progressDialog != null) {
			progressDialog.dismiss();

			setUpdatingDialogHidingOnResumeRequested(false);
		}
	}

	private boolean isPaused() {
		return isPaused;
	}

	private void setUpdatingDialogHidingOnResumeRequested(boolean isRequested) {
		isUpdatingDialogHidingOnResumeRequested = isRequested;
	}

	@Override
	public void onNetworkFail() {
		hideUpdatingProgressDialog();

		UserAlerter.alert(this, R.string.error_connection);
	}

	@Override
	public void onFailedUpdate() {
		hideUpdatingProgressDialog();

		UserAlerter.alert(this, R.string.error_unspecified);
	}

	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		Map<String, Object> instance = new HashMap<String, Object>();

		instance.put(LastInstanceKeys.SELECTED_TAB, getSelectedTabPosition());

		instance.put(LastInstanceKeys.DATABASE_UPDATE_CHECK_TASK, databaseUpdateCheckTask);
		instance.put(LastInstanceKeys.DATABASE_UPDATE_TASK, databaseUpdateTask);

		return instance;
	}

	private int getSelectedTabPosition() {
		return getSupportActionBar().getSelectedNavigationIndex();
	}

	@Override
	protected void onPause() {
		super.onPause();

		setPaused(true);
	}

	private void setPaused(boolean isPaused) {
		this.isPaused = isPaused;
	}

	@Override
	protected void onResume() {
		super.onResume();

		setPaused(false);

		if (isUpdatingDialogHidingOnResumeRequested()) {
			hideUpdatingProgressDialog();
		}
	}

	private boolean isUpdatingDialogHidingOnResumeRequested() {
		return isUpdatingDialogHidingOnResumeRequested;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_action_bar_home, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case R.id.menu_search_stations:
				callStationsSearch();
				return true;

			case R.id.menu_update_information:
				callDatabaseUpdate();
				return true;

			case R.id.menu_stations_map:
				callStationsMapActivity();
				return true;

			case R.id.menu_rate_application:
				callGooglePlay();
				return true;

			case R.id.menu_send_feedback:
				callSendingFeedback();
				return true;

			default:
				return super.onOptionsItemSelected(menuItem);
		}
	}

	private void callStationsSearch() {
		onSearchRequested();
	}

	private void callStationsMapActivity() {
		Intent intent = IntentFactory.createStationsMapIntent(this);
		startActivity(intent);
	}

	private void callGooglePlay() {
		try {
			Intent intent = IntentFactory.createGooglePlayIntent(buildAppGooglePlayUrl());
			startActivity(intent);
		}
		catch (ActivityNotFoundException e) {
			Intent intent = IntentFactory.createGooglePlayIntent(buildWebGooglePlayUrl());
			startActivity(intent);
		}
	}

	private String buildAppGooglePlayUrl() {
		return getString(R.string.url_app_google_play, getPackageName());
	}

	private String buildWebGooglePlayUrl() {
		return getString(R.string.url_web_google_play, getPackageName());
	}

	private void callSendingFeedback() {
		Intent intent = IntentFactory.createEmailIntent(getString(R.string.email_address_feedback),
			getString(R.string.email_subject_feedback));
		startActivity(Intent.createChooser(intent, null));
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem updatingActionBarButton = menu.findItem(R.id.menu_update_information);
		MenuItem searchActionBarButton = menu.findItem(R.id.menu_search_stations);

		if (isUpdatingAvailable) {
			updatingActionBarButton.setVisible(true);

			searchActionBarButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		else {
			updatingActionBarButton.setVisible(false);

			searchActionBarButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}

		return super.onPrepareOptionsMenu(menu);
	}
}
