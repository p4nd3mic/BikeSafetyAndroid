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
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * This shows how to create a simple activity with a map and a marker on the map.
 * <p>
 * Notice how we deal with the possibility that the Google Play services APK is not
 * installed/enabled/updated on a user's device.
 */
public class MainActivity extends android.support.v4.app.FragmentActivity {
    /**
     * Note that this may be null if the Google Play services APK is not available.
     */
    private GoogleMap mMap;
    private String[] addresses;
    Geocoder gCoder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BufferedReader in = null;
        BufferedWriter out = null;
        addresses = new String[200];
        gCoder = new Geocoder(this);
    	File aFile = new File("/test/test.txt");
    	//String filePath = getBaseContext().getFilesDir().getPath().toString() + "/fileName.txt";
    	//File f = new File(filePath);
        try {
			in = new BufferedReader(new InputStreamReader(getAssets().open("bike_locations.txt")));
			 //FileWriter filewriter = new FileWriter(aFile,true);
			//out = new BufferedWriter(new FileWriter(f));//getAssets().open("blank.txt")));
		
			String line;
			int i = 0;
        
        	//add lines to an array of strings.
			while ((line = in.readLine()) != null)
			{
			    addresses[i] = line.substring(3);
			    System.out.println(addresses[i]);
			    i++;
			}


		} catch (IOException e) {
			System.out.println(e);
		}
        
        setUpMapIfNeeded();
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
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView
     * MapView}) will show a prompt for the user to install/update the Google Play services APK on
     * their device.
     * <p>
     * A user can return to this Activity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the Activity may not have been
     * completely destroyed during this process (it is likely that it would only be stopped or
     * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
     * {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
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
     * This is where we can add markers or lines, add listeners or move the camera. 
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
    	
    	 List<Address> address = new ArrayList<Address>();
         System.out.println("branch 1");
         
         try {
         	
         	//api call for all addresses. Sends each address string to geocoder and returns an Address type.
         	//Slows app. need to store or run in background. The error print out in logcat
        	// is the difference between the number of addresses we are given and number of lat/lon we get.
         	for(int q = 1; q <= 125; q++) {
         		try {
         		address.addAll(gCoder.getFromLocationName(addresses[q],1));
         		System.out.println(address.get(q - 1));
     			//address.addAll(gCoder.getFromLocationName(addresses[1],1));

         		} catch (Exception e) {
         			System.out.println(e);
         		}
         	}
         	
         	//add markers based on lat long. 
         	for(int p= 0; p < 125; p++) {
         		double lat = address.get(p).getLatitude();
         		double lon = address.get(p).getLongitude();
         		System.out.println(lat);
         		System.out.println(lon);
         		
         		//if statement for philly lat long to get rid of extraneous values.
         		//if ((lat >= 34.0 && lat <= 41) && (lon >= 75 && lon <= 80)) {
         			mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title("Marker"));
         		//}
         	}
             
         } catch (Exception e) {
         	System.out.println(e);
         }


        mMap.setMyLocationEnabled(true);
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);
        Location location = service.getLastKnownLocation(provider);
        if (location != null){
            LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
            CameraPosition camPos = CameraPosition.fromLatLngZoom(userLocation, 15);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));
        }
        
        
       
    }
}
