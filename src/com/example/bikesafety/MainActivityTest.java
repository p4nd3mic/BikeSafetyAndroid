
	package com.example.bikesafety;

	import java.io.BufferedReader;
	import java.io.IOException;
	import java.io.InputStream;
	import java.io.InputStreamReader;
	import junit.framework.Assert;
	import android.content.Context;
	import android.content.res.AssetManager;
	import android.test.AndroidTestCase;
	import junit.framework.TestCase;
	import android.app.Activity;

	public class MainActivityTest extends android.test.InstrumentationTestCase {

		public MainActivityTest() {
			super();
		}

		protected void setUp() throws Exception {
			super.setUp();
		}

		protected void tearDown() throws Exception {
			super.tearDown();
		}
		
		//reads first line of bike_safety file to test coordinates
		public void testLatLong() {		
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader
						(getInstrumentation().getTargetContext().getResources().getAssets().open("bike_locations.txt")));
				String line;
				line = in.readLine();
				String[] coordinates = line.split(",");
				
				double lat = Double.parseDouble(coordinates[0]);
				double lon = Double.parseDouble(coordinates[1]);
				
				double expectedLat = 39.9503091;
				Assert.assertEquals(expectedLat, lat);
				
				double expectedLon = -75.1917456;
				Assert.assertEquals(expectedLon, lon);
			
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}


