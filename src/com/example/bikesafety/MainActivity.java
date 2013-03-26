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
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

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
	private HashMap<Marker, String> markerIDs;
	private static final LatLng thirtyEighthAndMarket = new LatLng(39.956685,
			-75.198031);
	private static final LatLng thirtyEighthAndSpruce = new LatLng(39.951409,
			-75.199129);
	private static final LatLng spruceAnd40th = new LatLng(39.951785,
			-75.203085);
	private static final LatLng spruceAnd42nd = new LatLng(39.952229,
			-75.206561);
	private static final LatLng pineAnd42nd = new LatLng(39.951211, -75.207159);
	private static final LatLng marketAnd40th = new LatLng(39.957211,-75.20195);


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Parse.initialize(this, "wllYXLfWfUbFoBpPBBGK2aLa9V5H0LaCkoKR3qfm",
				"mraFSkEryhjIgD3Td2pMY062zxyhKKjEeeu8DsOX");

		setContentView(R.layout.activity_main);
		setUpMapIfNeeded();

	}
	
	//@Override	
	//public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
	//	View view = inflater.inflate(R.layout.activity_main, container, false);
	//	return view;
	//}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		 MenuInflater inflater = getMenuInflater();
		    inflater.inflate(R.menu.activity_main, menu);
		    return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.menu_settings:
            	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.dot.state.pa.us/Internet/Bureaus/pdBikePed.nsf/infoAcknowledgements?OpenForm"));
            	startActivity(browserIntent);
                return true;
            case R.id.menu_photo_creds:
            	browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://mapicons.nicolasmollet.com/"));
            	startActivity(browserIntent);
                return true;

            default:
            return super.onOptionsItemSelected(item);
        }
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

	private void fetchBikeRacks(Location currentLocation) {

		ParseQuery query = new ParseQuery("BikeRack");
		query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
		query.findInBackground(new FindCallback() {
			@Override
			public void done(List<ParseObject> scoreList, ParseException e) {
				if (e == null) {
					for (ParseObject p : scoreList) {
						ParseGeoPoint point = p.getParseGeoPoint("location");
						double lat = point.getLatitude();
						double lon = point.getLongitude();
						Marker m = mMap.addMarker(new MarkerOptions()
								.position(new LatLng(lat, lon))
								.title(p.getString("address") + " >")
								.icon(BitmapDescriptorFactory
										.fromAsset("bicycle_shop.png")));
						markerIDs.put(m, p.getObjectId());
					}
				}
			}
		});
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

		markerIDs = new HashMap<Marker, String>();
		Location currentLocation = zoomAndCenterOnCurrentLocation();
		fetchBikeRacks(currentLocation);
		addTrolleyTracks();

		setUpConstructionSites();
		mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker mark) {

				showComments(markerIDs.get(mark));

			}
		});

	}

	private void addTrolleyTracks() {
		mMap.addPolyline((new PolylineOptions())
				.add(thirtyEighthAndMarket, thirtyEighthAndSpruce,
						spruceAnd40th, spruceAnd42nd, pineAnd42nd).width(5)
				.color(Color.RED));
		mMap.addPolyline((new PolylineOptions())
				.add(marketAnd40th, spruceAnd40th).width(5)
				.color(Color.RED));

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

	private void showComments(String marker_id) {
		Intent intent = new Intent(this, CommentActivity.class);
		intent.putExtra("com.example.bikesafety.ID", marker_id);
		startActivity(intent);
	}
}
