package com.example.bikesafety;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.PorterDuff;

public class GetDirections extends Activity implements OnClickListener {

	Button button1;
	Button button2;
	String marker_id;
	protected ParseObject rack;
	double to_lat;
	double to_lon;
	Location location;
	String address = "";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.get_directions);


		Parse.initialize(this, "wllYXLfWfUbFoBpPBBGK2aLa9V5H0LaCkoKR3qfm",
				"mraFSkEryhjIgD3Td2pMY062zxyhKKjEeeu8DsOX");
		
		marker_id = getIntent().getStringExtra("com.example.bikesafety.ID");
		location = getIntent().getParcelableExtra("com.example.bikesafety.location");
		ParseQuery query = new ParseQuery("BikeRack");
		query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
		query.getInBackground(marker_id, new GetCallback() {
			public void done(ParseObject p, ParseException e) {
				if (e == null) {
					rack = p;
		        	ParseGeoPoint point = rack.getParseGeoPoint("location");
					to_lat = point.getLatitude();
					to_lon = point.getLongitude();
					address = rack.getString("address");
					System.out.println(address);
					} else {
					}
				}
			});


		TextView text =  (TextView) findViewById(R.id.tv);
		text.setText("3500 Osler St");
        //text.setText(address);
        addListenerOnButton();

	}


	public void addListenerOnButton() {


		button1 = (Button) findViewById(R.id.button1);
		button1.getBackground().setColorFilter(0xFF00bfff,
				PorterDuff.Mode.MULTIPLY);
		
		button2 = (Button) findViewById(R.id.button2);
		button2.getBackground().setColorFilter(0xFF6dc066,
				PorterDuff.Mode.MULTIPLY);
		
		button1.setOnClickListener(this);
		button2.setOnClickListener(this);


	}
	@Override
	public void onClick(View v) {

	      switch (v.getId()) {
	         case R.id.button1: 
				showComments();
	          break;
	         case R.id.button2:
	        	directions();
	          break;
	      }
	   }

	private void showComments() {
		Intent intent = new Intent(this, CommentActivity.class);
		intent.putExtra("com.example.bikesafety.ID", marker_id);
		startActivity(intent);
	}
	
	private void directions() {
		 Double from_lat = location.getLatitude();
    	 Double from_lon = location.getLongitude();
    	 String url = "http://maps.google.com/maps?saddr=" + from_lat + "," + from_lon + "&daddr=" + to_lat + "," + to_lon;
    	 Intent app = new Intent(android.content.Intent.ACTION_VIEW,  Uri.parse(url));
    	 startActivity(app);
	}

}
