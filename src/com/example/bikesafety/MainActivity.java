package com.example.bikesafety;

/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * This shows how to create a simple activity with a map and a marker on the
 * map.
 * <p>
 * Notice how we deal with the possibility that the Google Play services APK is
 * not installed/enabled/updated on a user's device.
 */
public class MainActivity extends android.support.v4.app.FragmentActivity {
	/**
	 * Note that this may be null if the Google Play services APK is not
	 * available.
	 */
	private GoogleMap mMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Parse.initialize(this, "wllYXLfWfUbFoBpPBBGK2aLa9V5H0LaCkoKR3qfm",
				"mraFSkEryhjIgD3Td2pMY062zxyhKKjEeeu8DsOX");

		setContentView(R.layout.activity_main);
		setUpMapIfNeeded();

	}

	private void readLatLongFile(Location currentLocation) {

		ParseQuery query = new ParseQuery("BikeRack");
		query.findInBackground(new FindCallback() {
			@Override
			public void done(List<ParseObject> scoreList, ParseException e) {
				if (e == null) {
					for (ParseObject p : scoreList) {
						ParseGeoPoint point = p.getParseGeoPoint("location");
						double lat = point.getLatitude();
						double lon = point.getLongitude();
						mMap.addMarker(new MarkerOptions()
								.position(new LatLng(lat, lon))
								.title(p.getString("address"))
								.icon(BitmapDescriptorFactory
										.fromAsset("bicycle_shop.png")));
					}

				}
			}

		});

	}

	

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	/**
	 * Sets up the map if it is possible to do so (i.e., the Google Play
	 * services APK is correctly installed) and the map has not already been
	 * instantiated.. This will ensure that we only ever call
	 * {@link #setUpMap()} once when {@link #mMap} is not null.
	 * <p>
	 * If it isn't installed {@link SupportMapFragment} (and
	 * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt
	 * for the user to install/update the Google Play services APK on their
	 * device.
	 * <p>
	 * A user can return to this Activity after following the prompt and
	 * correctly installing/updating/enabling the Google Play services. Since
	 * the Activity may not have been completely destroyed during this process
	 * (it is likely that it would only be stopped or paused),
	 * {@link #onCreate(Bundle)} may not be called again so we should call this
	 * method in {@link #onResume()} to guarantee that it will be called.
	 */
	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				try {
					setUpMap();
				} catch (Exception e) {

				}
			}
		}
	}

	/**
	 * This is where we can add markers or lines, add listeners or move the
	 * camera.
	 * <p>
	 * This should only be called once and when we are sure that {@link #mMap}
	 * is not null.
	 */
	private void setUpMap() {

		Location currentLocation = zoomAndCenterOnCurrentLocation();
		readLatLongFile(currentLocation);

		setUpConstructionSites();
		// Add a listener for marker click events
		/*
		 * mMap.setOnMarkerClickListener( new GoogleMap.OnMarkerClickListener()
		 * {
		 * 
		 * @Override public boolean onMarkerClick(Marker arg0) { showComments();
		 * return false; } });
		 */
		mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker arg0) {
				showComments();

			}
		});

	}

	private void setUpConstructionSites() {
		mMap.addMarker(new MarkerOptions()
				.position(new LatLng(39.950905, -75.196033))
				.title("Construction")
				.icon(BitmapDescriptorFactory.fromAsset("construction.png")));
	}

	private Location zoomAndCenterOnCurrentLocation() {
		mMap.setMyLocationEnabled(true);
		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String provider = service.getBestProvider(criteria, false);
		Location location = service.getLastKnownLocation(provider);
		if (location != null) {
			LatLng userLocation = new LatLng(location.getLatitude(),
					location.getLongitude());
			CameraPosition camPos = CameraPosition.fromLatLngZoom(userLocation,
					15);
			mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));
		}
		return location;
	}

	private void showComments() {
		Intent intent = new Intent(this, CommentActivity.class);
		// intent.putExtra("ID", marker_id);
		startActivity(intent);
	}
}
