package app.android.bustime.ui;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import app.android.bustime.R;
import app.android.bustime.db.DbImportException;
import app.android.bustime.db.DbImporter;
import app.android.bustime.db.DbProvider;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;


public class HomeActivity extends SherlockFragmentActivity
{
	private static final String SAVED_INSTANCE_KEY_SELECTED_TAB = "selected_tab";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setUpTabs();

		if (savedInstanceState != null) {
			setSelectedTab(savedInstanceState.getInt(SAVED_INSTANCE_KEY_SELECTED_TAB, 0));
		}
	}

	private void setUpTabs() {
		ActionBar actionBar = getSupportActionBar();

		actionBar.addTab(buildRoutesTab());
		actionBar.addTab(buildStationsTab());
	}

	private ActionBar.Tab buildRoutesTab() {
		ActionBar.Tab tab = getSupportActionBar().newTab();

		tab.setText(getString(R.string.title_routes));
		tab.setTabListener(new TabListener(FragmentFactory.createRoutesFragment(this)));

		return tab;
	}

	private ActionBar.Tab buildStationsTab() {
		ActionBar.Tab tab = getSupportActionBar().newTab();

		tab.setText(getString(R.string.title_stations));
		tab.setTabListener(new TabListener(FragmentFactory.createStationsFragment(this)));

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

	private void setSelectedTab(int tabPosition) {
		getSupportActionBar().setSelectedNavigationItem(tabPosition);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_action_bar_home, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case R.id.menu_update:
				callDatabaseImport();
				return true;

			case R.id.menu_map:
				callStationsMapActivity();
				return true;

			default:
				return super.onOptionsItemSelected(menuItem);
		}
	}

	private void callDatabaseImport() {
		new ImportDatabaseTask().execute();
	}

	private class ImportDatabaseTask extends AsyncTask<Void, Void, String>
	{
		private ProgressDialogHelper progressDialogHelper;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			progressDialogHelper = new ProgressDialogHelper();
			progressDialogHelper.show(HomeActivity.this, R.string.loading_import);
		}

		@Override
		protected String doInBackground(Void... voids) {
			try {
				DbImporter dbImporter = new DbImporter(HomeActivity.this);
				dbImporter.importFromServer();
				DbProvider.getInstance().refreshDatabase(HomeActivity.this);
			}
			catch (DbImportException e) {
				return getString(R.string.error_unspecified);
			}

			return new String();
		}

		@Override
		protected void onPostExecute(String errorMessage) {
			super.onPostExecute(errorMessage);

			if (TextUtils.isEmpty(errorMessage)) {
				reSetUpTabs();
			}
			else {
				UserAlerter.alert(HomeActivity.this, errorMessage);
			}

			progressDialogHelper.hide();
		}
	}

	private void reSetUpTabs() {
		int selectedTabPosition = getSelectedTabPosition();

		tearDownTabs();
		setUpTabs();

		setSelectedTab(selectedTabPosition);
	}

	private int getSelectedTabPosition() {
		return getSupportActionBar().getSelectedNavigationIndex();
	}

	private void tearDownTabs() {
		getSupportActionBar().removeAllTabs();
	}

	private void callStationsMapActivity() {
		Intent callIntent = IntentFactory.createStationsMapIntent(this);
		startActivity(callIntent);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(SAVED_INSTANCE_KEY_SELECTED_TAB, getSelectedTabPosition());
	}
}
