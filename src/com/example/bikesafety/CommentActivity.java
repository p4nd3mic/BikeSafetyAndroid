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

import com.example.bikesafety.R;
import com.example.bikesafety.R.id;
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
	protected EditText input;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_comment);
		
		// Get the marker ID
		// String id = getIntent().getStringExtra("ID");
		
		// Header (address)
		TextView header = new TextView(this);
		header.setText("34th and Spruce St.");
		header.setGravity(0x11);
		header.setTextSize(20);
		
		// Comment input box
		input = new EditText(this);
		input.setHint("Leave a comment");
		
		// Submit button
		Button submit = new Button(this);
		submit.setText("Submit");
		submit.setGravity(0x11);
		submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                submitComment(input.getText().toString());
            }
        });
		
		// Footer (Comment submission)
		LinearLayout footer = new LinearLayout(this);
		footer.setOrientation(1);
		footer.addView(input, 0);
		footer.addView(submit, 1);
		
		// List of comments
		ListView lv = (ListView) findViewById(id.listview);
		lv.addHeaderView(header);
		lv.addFooterView(footer);
		
		// Load the comments
		loadComments();
	}
	
	// Read comments from txt
	public void loadComments() {
		// Read the comments
		ArrayList<String> comments = new ArrayList<String>();
		try {
			String file = getFilesDir() + File.separator + "comments.txt";
			FileInputStream fin = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fin);
			BufferedReader in = new BufferedReader(isr);
			while (in.ready()) {
				comments.add(in.readLine());
			}
			in.close();
			isr.close();
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// ArrayAdapter for populating the ListView
		adapter = new ArrayAdapter<String>(this, 
		        android.R.layout.simple_list_item_1, comments);
		
		// Assign the adapter to the ListView
		ListView lv = (ListView) findViewById(id.listview);
		lv.setAdapter(adapter);
	}
	
	// Write a comment to txt
	public void submitComment(String comment) {
		// Check for invalid input
		if (comment == null || comment.trim().length() <= 0) {
			// TODO Display an error message
			
		} else {
			// Write the new comment to the txt
			try {
				String file = getFilesDir() + File.separator + "comments.txt";
				FileOutputStream fout = new FileOutputStream(file, true);
				BufferedOutputStream bout = new BufferedOutputStream(fout);
				PrintStream ps = new PrintStream(bout);
				ps.println(comment);
				ps.close();
				bout.close();
				fout.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// Reload the comments
			loadComments();
			
			// Clear the textbox
			input.setText(null);
		}
	}

}
