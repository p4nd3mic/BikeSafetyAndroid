package com.example.bikesafety;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

public class CommentActivity extends ExpandableListActivity {

	protected ExpandableListAdapter adapter;
	protected ArrayList<HashMap<String, String>> groupData;
	protected ArrayList<ArrayList<HashMap<String, String>>> childData;
	protected ParseObject rack;
	protected ParseObject currentComment;
	protected HashMap<String, ParseObject> mapOfComments;
	private final String applicationId = "wllYXLfWfUbFoBpPBBGK2aLa9V5H0LaCkoKR3qfm";
	private final String clientKey = "mraFSkEryhjIgD3Td2pMY062zxyhKKjEeeu8DsOX";
    
	private ProgressBar mProgress;
	
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_comment, menu);

		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent safety = new Intent(this, SafetyTips.class);
			startActivity(safety);
			return true;
		case R.id.menu_directions:
			directions();

		
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	private void directions() {
		double to_lat = rack.getParseGeoPoint("location").getLatitude();
		double to_lng = rack.getParseGeoPoint("location").getLongitude();
		Location location = getCurrentLocation();
		Double from_lat = location.getLatitude();
		Double from_lon = location.getLongitude();
		String url = "http://maps.google.com/maps?saddr=" + from_lat + ","
				+ from_lon + "&daddr=" + to_lat + "," + to_lng
				+ "&lci=bike&dirflg=b";
		Intent app = new Intent(android.content.Intent.ACTION_VIEW,
				Uri.parse(url));
		startActivity(app);
	}
	
	private Location getCurrentLocation() {
		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String provider = service.getBestProvider(criteria, false);
		Location location = service.getLastKnownLocation(provider);
		return location;
	}
	
	private class ExpandedListAdapter extends SimpleExpandableListAdapter {

		public ExpandedListAdapter(Context context,
				List<? extends Map<String, ?>> groupData, int groupLayout,
				String[] groupFrom, int[] groupTo,
				List<? extends List<? extends Map<String, ?>>> childData,
				int childLayout, String[] childFrom, int[] childTo) {
			super(context, groupData, groupLayout, groupFrom, groupTo, childData,
					childLayout, childFrom, childTo);
		}
		
		// Code adapted from: http://stackoverflow.com/questions/3988337/how-to-keep-expandablelistview-in-expanded-status
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		    View v = super.getGroupView(groupPosition, isExpanded, convertView, parent);
		    ExpandableListView eLV = (ExpandableListView) parent;
		    eLV.expandGroup(groupPosition);
		    return v;
		}
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Parse.initialize(this, applicationId,clientKey);
		
		setContentView(R.layout.activity_comment);
		
		mapOfComments = new HashMap<String,ParseObject>();
		
		// Get the marker ID
		String marker_id = getIntent().getStringExtra("com.example.bikesafety.ID");
		
		// Add header to ListView
		ExpandableListView lv = getExpandableListView();
		lv.addHeaderView(View.inflate(this, R.layout.comment_header, null));
		
		// Get the bikeRack
		ParseQuery query = new ParseQuery("BikeRack");
		query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
		query.getInBackground(marker_id, new GetCallback() {
			public void done(ParseObject p, ParseException e) {
				if (e == null) {
					rack = p;
					setup();
				}
			}
		});
	}
	
	public void setup() {
		// Load the comments from the database
		getComments();
		
		// Create and set the ListAdapter
		setListAdapter(getAdapter());
		
		// Setup the ListView
		setupListView();
		
		// Get rid of the progress bar
		mProgress = (ProgressBar) findViewById(R.id.progress_bar);
        mProgress.setVisibility(View.GONE);
	}
	
	private void getComments() {
		groupData = new ArrayList<HashMap<String, String>>();
		childData = new ArrayList<ArrayList<HashMap<String, String>>>();
		HashMap<String,ArrayList<HashMap<String, String>>> replies =
				new HashMap<String,ArrayList<HashMap<String, String>>>();
		
		// Query to retrieve responses
		ParseQuery query = new ParseQuery("Reply");
		query.whereEqualTo("bikeRack", rack);
		query.orderByAscending("createdAt");
		query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
		
		// Run the query
		try {
			for (ParseObject p : query.find()) {
				HashMap<String, String> reply = new HashMap<String, String>();
				
				String content = p.getString("content");
				String created = p.getCreatedAt().toString();
				String parent = p.getParseObject("comment").getObjectId();
				
				reply.put("content",content);
			    reply.put("date",created);
			    
			    if (replies.get(parent) == null) {
			    	replies.put(parent, new ArrayList<HashMap<String, String>>());
			    }
			    
			    replies.get(parent).add(reply);
			    
			}
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		
		// Query to retrieve comments
		query = new ParseQuery("Comment");
		query.whereEqualTo("bikeRack", rack);
		query.orderByDescending("createdAt");
		query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
		
		// Run the query
		try {
			for (ParseObject p : query.find()) {
				HashMap<String, String> comment = new HashMap<String, String>();
				
				String body = p.getString("body");
				String created = p.getCreatedAt().toString();
				String id = p.getObjectId();
				
			    comment.put("body",body);
			    comment.put("date",created);
			    comment.put("id", id);
			    
			    mapOfComments.put(id, p);
			    
			    groupData.add(comment);
			    
			    ArrayList<HashMap<String, String>> r = replies.get(p.getObjectId());
			    if (r == null) r = new ArrayList<HashMap<String,String>>();
			    childData.add(r);
			}
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
	}
	
	private SimpleExpandableListAdapter getAdapter() {
		// Code adapted from: http://mylifewithandroid.blogspot.com/2008/05/expandable-lists.html
		SimpleExpandableListAdapter adapter =
			new ExpandedListAdapter(
				this,
				groupData,	// groupData describes the first-level entries
				R.layout.parent_row,	// Layout for the first-level entries
				new String[]{"body", "date"},	// Key in the groupData maps to display
				new int[]{R.id.parent_body, R.id.parent_date},	// Data under the keys above go into these TextViews
				childData,	// childData describes second-level entries
				R.layout.child_row,	// Layout for second-level entries
				new String[]{"content", "date"},	// Keys in childData maps to display
				new int[] {R.id.child_body, R.id.child_date}	// Data under the keys above go into these TextViews
			);
		return adapter;
	}
	
	private void setupListView() {
		// Load the address & building name
        TextView addressText = (TextView) findViewById(R.id.comment_address);
        TextView buildingText = (TextView) findViewById(R.id.comment_building);
		addressText.setText(rack.getString("address"));
		buildingText.setText(rack.getString("buildingName"));
		
		// Listener for clicking comments
		ExpandableListView lv = getExpandableListView();
		lv.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				String parent_id = groupData.get(groupPosition).get("id");
				submitReply(parent_id);
				return true;
			}
		});
		
		// Listener for clicking replies
		lv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				String parent_id = groupData.get(groupPosition).get("id");
				submitReply(parent_id);
				return true;
			}
		});
		
		// Get rid of the group indicator
		lv.setGroupIndicator(null);
		
		// Fix problem with losing focus when scrolling
		lv.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// Do nothing!
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				ExpandableListView lv = getExpandableListView();
				lv.requestFocusFromTouch();
			}
		});
	}
	
	// Write a comment to database
	public void submitComment(View view) {
		// Get the comment input box
		EditText inputBody = (EditText) findViewById(R.id.comment_edit);
		String body = inputBody.getText().toString();
		
		// Check for invalid input
		if (body == null || body.trim().length() <= 0) {
			inputBody.setHint(R.string.comment_invalid);
			inputBody.setText(null);
			return;
		}
        
		mProgress.setVisibility(View.VISIBLE);
		
		// Write the new comment to the database
		ParseObject p = new ParseObject("Comment");
		p.put("body", body);
		p.put("bikeRack", rack);
		p.saveEventually(new SaveCallback() {
			public void done(ParseException e) {
				setup();
			}
		});
		
		// Clear the textbox
		inputBody.setHint(R.string.comment_hint);
		inputBody.setText(null);
	}
	
	// Write a reply to database
	public void submitReply(String comment_id) {
		// Code adapted from: http://www.androidsnippets.com/prompt-user-input-with-an-alertdialog
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(R.string.comment_reply_title);

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		currentComment = mapOfComments.get(comment_id);
		
		alert.setPositiveButton(R.string.comment_submit, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			String value = input.getText().toString();
			
			// Check for invalid input
			if (value == null || value.trim().length() <= 0) {
				return;
			}
			
			mProgress.setVisibility(View.VISIBLE);
			
		  	// Write the new comment to the database
			ParseObject p = new ParseObject("Reply");
			p.put("content", value);
			p.put("bikeRack", rack);
			p.put("comment", currentComment);
			p.saveEventually(new SaveCallback() {
				public void done(ParseException e) {
					setup();
				}
			});
		  }
		});

		alert.setNegativeButton(R.string.comment_cancel, new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();
	}

}
