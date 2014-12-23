package ru.ming13.bustime.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import ru.ming13.bustime.R;
import ru.ming13.bustime.model.Stop;
import ru.ming13.bustime.util.Bartender;
import ru.ming13.bustime.util.Fragments;

public class StopMapFragment extends SupportMapFragment
{
	private static final class Ui
	{
		private Ui() {
		}

		public static final boolean CURRENT_LOCATION_ENABLED = true;
		public static final boolean ZOOM_ENABLED = true;
	}

	private static final class Defaults
	{
		private Defaults() {
		}

		public static final int ZOOM = 15;
	}

	public static StopMapFragment newInstance(@NonNull Stop stop) {
		StopMapFragment fragment = new StopMapFragment();

		fragment.setArguments(buildArguments(stop));

		return fragment;
	}

	private static Bundle buildArguments(Stop stop) {
		Bundle arguments = new Bundle();

		arguments.putParcelable(Fragments.Arguments.STOP, stop);

		return arguments;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setUpMap();
		setUpStop();

		setUpCameraPosition();
	}

	private void setUpMap() {
		setUpUi();
	}

	private void setUpUi() {
		GoogleMap map = getMap();

		map.setMyLocationEnabled(Ui.CURRENT_LOCATION_ENABLED);
		map.getUiSettings().setMyLocationButtonEnabled(Ui.CURRENT_LOCATION_ENABLED);

		map.getUiSettings().setZoomControlsEnabled(Ui.ZOOM_ENABLED);

		Bartender bartender = Bartender.at(getActivity());
		map.setPadding(
			bartender.getLeftUiPadding(),
			bartender.getTopUiPadding(),
			bartender.getRightUiPadding(),
			bartender.getBottomUiPadding());
	}

	private void setUpStop() {
		setUpStopMarker();
	}

	private void setUpStopMarker() {
		GoogleMap map = getMap();

		Marker stopMarker = map.addMarker(buildStopMarkerOptions());
		stopMarker.showInfoWindow();
	}

	private MarkerOptions buildStopMarkerOptions() {
		Stop stop = getStop();

		return new MarkerOptions()
			.title(stop.getName())
			.snippet(stop.getDirection())
			.position(new LatLng(stop.getLatitude(), stop.getLongitude()))
			.icon(BitmapDescriptorFactory.defaultMarker(getStopMarkerHue()));
	}

	private Stop getStop() {
		return getArguments().getParcelable(Fragments.Arguments.STOP);
	}

	private float getStopMarkerHue() {
		return getResources().getInteger(R.integer.hue_primary);
	}

	private void setUpCameraPosition() {
		getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(getDefaultLocation(), Defaults.ZOOM));
	}

	private LatLng getDefaultLocation() {
		Stop stop = getStop();

		return new LatLng(stop.getLatitude(), stop.getLongitude());
	}
}
