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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
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

/*
 * displays a map of bike parking spots, construction sites, 
 * and trolley tracks near Penn Campus
 */
public class MapActivity extends android.support.v4.app.FragmentActivity {

	private GoogleMap mMap;
	private HashMap<Marker, String> mMarkerIDs;
	private static final LatLng thirtyEighthAndMarket = new LatLng(39.956685,
			-75.198031);
	private static final LatLng thirtyEighthAndSpruce = new LatLng(39.951409,
			-75.199129);
	private static final LatLng spruceAnd40th = new LatLng(39.951785,
			-75.203085);
	private static final LatLng spruceAnd42nd = new LatLng(39.952229,
			-75.206561);
	private static final LatLng pineAnd42nd = new LatLng(39.951211, -75.207159);
	private static final LatLng marketAnd40th = new LatLng(39.957211, -75.20195);
	private final String FILENAME_CONSTRUCTION = "caution.png";
	private final String FILENAME_UNCOVERED = "cycling.png";
	private final String FILENAME_COVERED = "cycling_covered.png";
	private final String applicationId = "wllYXLfWfUbFoBpPBBGK2aLa9V5H0LaCkoKR3qfm";
	private final String clientKey = "mraFSkEryhjIgD3Td2pMY062zxyhKKjEeeu8DsOX";
	private Geocoder mGeoCoder;
	private FragmentManager mFragmentManager;
	private Bundle mFragments;
	private Object mViewPager;
	private TextView mTextView;
	private ListView mListView;
	private MapView mView;
	private LinearLayout layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Parse.initialize(this, applicationId, clientKey);
		mTextView = (TextView) findViewById(R.id.text);
		mListView = (ListView) findViewById(R.id.list);
		mGeoCoder = new Geocoder(this);

		setUpMapIfNeeded();
		handleIntent(getIntent());

	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {

		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			// handles a click on a search suggestion; launches activity to show
			// word
			intent.getData();
		} else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			System.out.println("action search");
			String query = intent.getStringExtra(SearchManager.QUERY);
	        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
	                SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
	        suggestions.saveRecentQuery(query, null);
			showResults(query);
			// handles a search query
			// Intent searchIntent = new Intent(this, SearchActivity.class);
			// searchIntent.setData(intent.getData());
			// startActivity(searchIntent);

		}
	}

	private void replace() {

		// Replace the fragment using a transaction.
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

		// transaction.replace(R.id.map_container, search);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	/*
	 * public void swapFragments() { FragmentManager fm =
	 * getSupportFragmentManager(); FragmentTransaction transaction =
	 * fm.beginTransaction(); if (listFragment.isVisible()) {
	 * transaction.hide(listFragment); if (mapFragment.isAdded()) {
	 * transaction.show(mapFragment); } else { transaction.add(R.id.root,
	 * mapFragment); } } else { transaction.hide(mapFragment);
	 * transaction.show(listFragment); } transaction.commit(); }
	 */

	/**
	 * Searches the dictionary and displays results for the given query.
	 * 
	 * @param query
	 *            The search query
	 */
	@SuppressLint("NewApi")
	private void showResults(String query) {
		try {
			
			double lowerLeftLatitude = 39.9;
			double lowerLeftLongitude = -75.26;
			double upperRightLongitud = 40;
			double upperRightLatitude = -75.1;
			ArrayList<Address> locationResults = (ArrayList<Address>) mGeoCoder
					.getFromLocationName(query, 15,  lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitud);
			

			
			if (locationResults.isEmpty())
				System.out.println("empty");
			
			else{
				for (Address addr : locationResults)
					if (addr.getAddressLine(1).equals("Philadelphia, PA")){
						System.out.println(addr.getAddressLine(1));
						zoomAndCenterOnLocation(addr.getLatitude(), addr.getLongitude());
						break;
					}
										
			}
			//this.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder)

			String[] fromColumns = new String[] {
					SearchManager.SUGGEST_COLUMN_TEXT_1,
					SearchManager.SUGGEST_COLUMN_TEXT_2 };
			int[] toViews = new int[] { R.id.search_entry, R.id.address };
			SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this, R.layout.result, null,
					fromColumns, toViews, 0);
			mListView.setAdapter(mAdapter);
			mTextView.setText(query);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Define the on-click listener for the list items
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Build the Intent used to open WordActivity with a specific
				// word Uri

			}
		});

		// Address addr = locationResults.get(0);
		// zoomAndCenterOnLocation(addr.getLatitude(), addr.getLongitude());

	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search)
				.getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent safety = new Intent(this, SafetyTips.class);
			startActivity(safety);
			return true;
		case R.id.menu_photo_creds:
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://mapicons.nicolasmollet.com/"));
			startActivity(browserIntent);
			return true;
		case R.id.menu_search:
			onSearchRequested();
			// replace();
			// Intent search = new Intent(this, SearchActivity.class);
			// startActivity(search);

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void fetchBikeRacks() {

		ParseQuery query = new ParseQuery("BikeRack");
		query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
		query.findInBackground(new FindCallback() {
			@Override
			public void done(List<ParseObject> rackList, ParseException e) {
				if (e == null) {
					for (ParseObject rack : rackList) {
						String icon;
						if (rack.getBoolean("covered"))
							icon = FILENAME_COVERED;
						else
							icon = FILENAME_UNCOVERED;
						ParseGeoPoint point = rack.getParseGeoPoint("location");
						double lat = point.getLatitude();
						double lng = point.getLongitude();
						LatLng position = new LatLng(lat, lng);
						String title = rack.getString("address");
						Marker marker = addMarker(icon, position, title);
						mMarkerIDs.put(marker, rack.getObjectId());
					}
				}
			}
		});
	}

	private void getDirections(String marker_id) {
		Intent getDirections = new Intent(this, GetDirections.class);
		getDirections.putExtra("com.example.bikesafety.ID", marker_id);
		Location location = getCurrentLocation();
		if (location != null)
			zoomAndCenterOnLocation(location.getLatitude(),
					location.getLongitude());
		getDirections.putExtra("com.example.bikesafety.location", location);
		startActivity(getDirections);

	}

	// helper method for fetchBikeRacks
	private Marker addMarker(String filename, LatLng position, String title) {
		MarkerOptions options = new MarkerOptions();
		options.icon(BitmapDescriptorFactory.fromAsset(filename));
		options.title(title + " >");
		options.position(position);
		return mMap.addMarker(options);
	}

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
	 * This should only be called once and when we are sure that {@link #mMap}
	 * is not null.
	 */
	private void setUpMap() {
		mMarkerIDs = new HashMap<Marker, String>();

		Location location = getCurrentLocation();
		if (location != null)
			zoomAndCenterOnLocation(location.getLatitude(),
					location.getLongitude());
		fetchBikeRacks();
		addTrolleyTracks();
		setUpConstructionSites();
		mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker mark) {
<<<<<<< HEAD
				getDirections(mMarkerIDs.get(mark));
=======
				getDirections(markerIDs.get(mark));
				//showComments(markerIDs.get(mark));
>>>>>>> bbcc44acc6e23dc9a77767c823cd9c286021b206
				return true;
			}
		});
	}

	private void addTrolleyTracks() {
		mMap.addPolyline((new PolylineOptions())
				.add(thirtyEighthAndMarket, thirtyEighthAndSpruce,
						spruceAnd40th, spruceAnd42nd, pineAnd42nd).width(5)
				.color(Color.RED));
		mMap.addPolyline((new PolylineOptions())
				.add(marketAnd40th, spruceAnd40th).width(5).color(Color.RED));

	}

	private void setUpConstructionSites() {
		mMap.addMarker(new MarkerOptions()
				.position(new LatLng(39.950905, -75.196033))
				.title("west-bound bike lane closed between 36th and 37th")
				.icon(BitmapDescriptorFactory.fromAsset(FILENAME_CONSTRUCTION)));
	}

	private void zoomAndCenterOnLocation(double lat, double lng) {
		mMap.setMyLocationEnabled(true);
		LatLng userLocation = new LatLng(lat, lng);
		CameraPosition camPos = CameraPosition.fromLatLngZoom(userLocation, 15);
		mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));
	}

	private Location getCurrentLocation() {
		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String provider = service.getBestProvider(criteria, false);
		Location location = service.getLastKnownLocation(provider);
		return location;
	}

	private void showComments(String marker_id) {
		Intent intent = new Intent(this, CommentActivity.class);
		intent.putExtra("com.example.bikesafety.ID", marker_id);
		startActivity(intent);
	}
	
	private void getDirections(String marker_id) {
		Intent getDirections = new Intent(this, GetDirections.class);
		getDirections.putExtra("com.example.bikesafety.ID", marker_id);
		Location location = zoomAndCenterOnCurrentLocation();
		getDirections.putExtra("com.example.bikesafety.location", location);
		startActivity(getDirections);
		
	}
}
