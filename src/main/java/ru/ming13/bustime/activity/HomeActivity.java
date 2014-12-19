package ru.ming13.bustime.activity;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import icepick.Icepick;
import icepick.Icicle;
import ru.ming13.bustime.R;
import ru.ming13.bustime.adapter.TabPagerAdapter;
import ru.ming13.bustime.bus.BusEventsCollector;
import ru.ming13.bustime.bus.BusProvider;
import ru.ming13.bustime.bus.DatabaseUpdateAvailableEvent;
import ru.ming13.bustime.bus.DatabaseUpdateFinishedEvent;
import ru.ming13.bustime.bus.RouteSelectedEvent;
import ru.ming13.bustime.bus.StopLoadedEvent;
import ru.ming13.bustime.bus.StopSelectedEvent;
import ru.ming13.bustime.fragment.RoutesFragment;
import ru.ming13.bustime.fragment.StopsFragment;
import ru.ming13.bustime.model.Route;
import ru.ming13.bustime.model.Stop;
import ru.ming13.bustime.provider.BusTimeContract;
import ru.ming13.bustime.task.DatabaseUpdateCheckingTask;
import ru.ming13.bustime.task.DatabaseUpdatingTask;
import ru.ming13.bustime.task.StopLoadingTask;
import ru.ming13.bustime.util.Fragments;
import ru.ming13.bustime.util.Frames;
import ru.ming13.bustime.util.Intents;
import ru.ming13.bustime.util.MapsUtil;
import ru.ming13.bustime.util.ViewDirector;
import ru.ming13.bustime.view.TabLayout;

public class HomeActivity extends ActionBarActivity implements ActionClickListener
{
	@InjectView(R.id.toolbar)
	Toolbar toolbar;

	@InjectView(R.id.layout_tabs)
	TabLayout tabLayout;

	@InjectView(R.id.pager_tabs)
	ViewPager tabPager;

	@Icicle
	boolean isDatabaseUpdateDone;

	@Icicle
	boolean isProgressVisible;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		setUpInjections();

		setUpSavedState(savedInstanceState);

		setUpUi();

		setUpDatabaseUpdate();
	}

	private void setUpInjections() {
		ButterKnife.inject(this);
	}

	private void setUpSavedState(Bundle state) {
		Icepick.restoreInstanceState(this, state);
	}

	private void setUpUi() {
		setUpToolbar();

		if (Frames.at(this).areAvailable()) {
			setUpFrames();
		} else {
			setUpTabs();
		}

		setUpProgress();
	}

	private void setUpToolbar() {
		setSupportActionBar(toolbar);
	}

	private void setUpFrames() {
		Frames.at(this).setLeftFrameTitle(getString(R.string.title_routes));
		Frames.at(this).setRightFrameTitle(getString(R.string.title_stops));

		Fragments.Operator.at(this).set(RoutesFragment.newInstance(), R.id.container_left_frame);
		Fragments.Operator.at(this).set(StopsFragment.newInstance(), R.id.container_right_frame);
	}

	private void setUpTabs() {
		tabPager.setAdapter(new TabPagerAdapter(this, getSupportFragmentManager()));
		tabLayout.setUpTabPager(getSupportActionBar().getThemedContext(), tabPager);
	}

	private void setUpProgress() {
		if (isProgressVisible) {
			showProgress();
		}
	}

	private void showProgress() {
		ViewDirector.of(this, R.id.animator).show(R.id.progress);

		isProgressVisible = true;
	}

	private void setUpDatabaseUpdate() {
		if (!isDatabaseUpdateDone) {
			DatabaseUpdateCheckingTask.execute(this);

			isDatabaseUpdateDone = true;
		}
	}

	@Subscribe
	public void onDatabaseUpdateAvailable(DatabaseUpdateAvailableEvent event) {
		showDatabaseUpdateBanner();
	}

	private void showDatabaseUpdateBanner() {
		Snackbar.with(this)
			.duration(Snackbar.SnackbarDuration.LENGTH_LONG)
			.text(R.string.message_updates)
			.actionLabel(R.string.button_download)
			.actionColorResource(R.color.background_bar)
			.actionListener(this)
			.show(this);
	}

	@Override
	public void onActionClicked(Snackbar snackbar) {
		startDatabaseUpdate();
	}

	private void startDatabaseUpdate() {
		showProgress();

		DatabaseUpdatingTask.execute(this);
	}

	@Subscribe
	public void onDatabaseUpdateFinished(DatabaseUpdateFinishedEvent event) {
		finishDatabaseUpdate();
	}

	private void finishDatabaseUpdate() {
		hideProgress();
	}

	private void hideProgress() {
		if (Frames.at(this).areAvailable()) {
			ViewDirector.of(this, R.id.animator).show(R.id.layout_frames);
		} else {
			ViewDirector.of(this, R.id.animator).show(R.id.content);
		}

		isProgressVisible = false;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			startShowingSearchResult(intent);
		}
	}

	private void startShowingSearchResult(Intent searchResultIntent) {
		long stopId = BusTimeContract.Stops.getStopsSearchId(searchResultIntent.getData());

		StopLoadingTask.execute(this, stopId);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_bar_home, menu);

		setUpStopsSearch(menu);
		setUpStopsMap(menu);

		return super.onCreateOptionsMenu(menu);
	}

	private void setUpStopsSearch(Menu menu) {
		SearchView stopsSearchView = getStopsSearchView(menu);

		setUpStopsSearchInformation(stopsSearchView);
		setUpStopsSearchView(stopsSearchView);
	}

	private SearchView getStopsSearchView(Menu menu) {
		MenuItem stopsSearchMenuItem = menu.findItem(R.id.menu_stops_search);

		return (SearchView) MenuItemCompat.getActionView(stopsSearchMenuItem);
	}

	private void setUpStopsSearchInformation(SearchView stopsSearchView) {
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

		stopsSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	}

	private void setUpStopsSearchView(SearchView stopsSearchView) {
		LinearLayout stopsSearchPlate = (LinearLayout) stopsSearchView.findViewById(R.id.search_plate);
		EditText stopsSearchQueryEdit = (EditText) stopsSearchView.findViewById(R.id.search_src_text);

		stopsSearchPlate.setBackgroundResource(R.drawable.abc_textfield_search_material);
		stopsSearchQueryEdit.setHintTextColor(getResources().getColor(R.color.text_hint_search));
	}

	private void setUpStopsMap(Menu menu) {
		if (MapsUtil.with(this).areMapsHardwareAvailable()) {
			return;
		}

		MenuItem stopsMapMenuItem = menu.findItem(R.id.menu_stops_map);

		stopsMapMenuItem.setVisible(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case R.id.menu_stops_map:
				startStopsMapActivity();
				return true;

			case R.id.menu_rate_application:
				startApplicationRating();
				return true;

			case R.id.menu_send_feedback:
				startFeedbackSending();
				return true;

			default:
				return super.onOptionsItemSelected(menuItem);
		}
	}

	private void startStopsMapActivity() {
		if (MapsUtil.with(this).areMapsSoftwareAvailable()) {
			Intent intent = Intents.Builder.with(this).buildStopsMapIntent();
			startActivity(intent);
		} else {
			MapsUtil.with(this).showErrorDialog();
		}
	}

	private void startApplicationRating() {
		try {
			Intent intent = Intents.Builder.with(this).buildGooglePlayAppIntent();
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Intent intent = Intents.Builder.with(this).buildGooglePlayWebIntent();
			startActivity(intent);
		}
	}

	private void startFeedbackSending() {
		Intent intent = Intents.Builder.with(this).buildFeedbackIntent();

		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			startActivity(Intent.createChooser(intent, null));
		}
	}

	@Subscribe
	public void onRouteSelected(RouteSelectedEvent event) {
		startRouteStopsActivity(event.getRoute());
	}

	private void startRouteStopsActivity(Route route) {
		Intent intent = Intents.Builder.with(this).buildRouteStopsIntent(route);
		startActivity(intent);
	}

	@Subscribe
	public void onStopSelected(StopSelectedEvent event) {
		startStopRoutesActivity(event.getStop());
	}

	@Subscribe
	public void onStopLoaded(StopLoadedEvent event) {
		startStopRoutesActivity(event.getStop());
	}

	private void startStopRoutesActivity(Stop stop) {
		Intent intent = Intents.Builder.with(this).buildStopRoutesIntent(stop);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();

		BusProvider.getBus().register(this);

		BusEventsCollector.getInstance().postCollectedEvents();
	}

	@Override
	protected void onPause() {
		super.onPause();

		BusProvider.getBus().unregister(this);

		BusProvider.getBus().register(BusEventsCollector.getInstance());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		tearDownState(outState);
	}

	private void tearDownState(Bundle state) {
		Icepick.saveInstanceState(this, state);
	}
}
