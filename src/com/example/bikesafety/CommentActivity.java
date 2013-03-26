package com.example.bikesafety;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.bikesafety.R;
import com.example.bikesafety.R.id;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.PushService;
import com.parse.SaveCallback;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class CommentActivity extends Activity {

	protected ArrayAdapter<String> adapter;
	protected ArrayList<String> comments;
	protected EditText inputTitle;
	protected EditText inputBody;
	protected TextView header;
	protected ParseObject rack;
	
	/*private class Comment {
		private String body;
		private String title;
		private Date created;
		
		private Comment(String title, String body, Date created) {
			this.body = body;
			this.title = title;
			this.created = created;
		}
	}*/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Parse.initialize(this, "wllYXLfWfUbFoBpPBBGK2aLa9V5H0LaCkoKR3qfm",
				"mraFSkEryhjIgD3Td2pMY062zxyhKKjEeeu8DsOX");
		
		setContentView(R.layout.activity_comment);
		
		// Get the marker ID
		String marker_id = getIntent().getStringExtra("com.example.bikesafety.ID");
		
		// Load the UI
		loadUI();
		
		// Get the bikeRack
		ParseQuery query = new ParseQuery("BikeRack");
		query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
		query.getInBackground(marker_id, new GetCallback() {
			public void done(ParseObject p, ParseException e) {
				if (e == null) {
					rack = p;
					loadData();
					} else {
						// TODO: do something
					}
				}
			});
	}
	
	public void loadUI() {
		// Set up header
		header = new TextView(this);
		header.setGravity(0x11);
		header.setTextSize(20);
		
		// Comment title input box
		inputTitle = new EditText(this);
		inputTitle.setHint("Title");
		
		// Comment input box
		inputBody = new EditText(this);
		inputBody.setHint("Leave a comment");
		
		// Submit button
		Button submit = new Button(this);
		submit.setText("Submit");
		submit.setGravity(0x11);
		submit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
					String title = inputTitle.getText().toString();
					String body = inputBody.getText().toString();
					submitComment(title, body);
				}
			});
		
		// Footer (Comment submission)
		LinearLayout footer = new LinearLayout(this);
		footer.setOrientation(1);
		footer.addView(inputTitle, 0);
		footer.addView(inputBody, 1);
		footer.addView(submit, 2);
		
		// List view for comments
		ListView lv = (ListView) findViewById(id.listview);
		lv.addHeaderView(header);
		lv.addFooterView(footer);
		
		// Assign the adapter to the ListView
		comments = new ArrayList<String>();
		adapter = new ArrayAdapter<String>(this, 
		        android.R.layout.simple_list_item_1, comments);
		lv.setAdapter(adapter);
	}
	
	public void loadData() {
		// Load the address
		header.setText(rack.getString("address"));
		
		// List of comments
		comments.clear();
		
		// Query to retrieve comments
		ParseQuery query = new ParseQuery("Comment");
		query.whereEqualTo("bikeRack", rack);
		query.orderByDescending("createdAt");
		query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
		
		// Run the query
		query.findInBackground(new FindCallback() {
		public void done(List<ParseObject> list, ParseException e) {
			if (e == null) {
				for (ParseObject p : list) {
					String body = p.getString("body");
		    		String title = p.getString("title");
		    		Date created = p.getCreatedAt();
		    		System.out.println(body);
		    		comments.add(title+'\n'+body+'\n'+created.toString());
		    		adapter.notifyDataSetChanged();
		    	}
		    } else {
		    	// TODO: do something
		    	System.out.println("AAAAAAAH");
		    }
		  }
		});
	}
	
	// Write a comment to database
	public void submitComment(String title, String body) {
		// Check for invalid input
		if (body == null || body.trim().length() <= 0 ||
				title == null || title.trim().length() <= 0) {
			// TODO Display an error message
			
		} else {
			// Write the new comment to the database
			ParseObject p = new ParseObject("Comment");
			p.put("title", title);
			p.put("body", body);
			p.put("bikeRack", rack);
			p.saveEventually(new SaveCallback() {
				public void done(ParseException e) { 
					loadData();
				}
			});
			
			// Clear the textboxes
			inputTitle.setText(null);
			inputBody.setText(null);
		}
	}

}
