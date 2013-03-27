package com.example.bikesafety;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class SafetyTips extends Activity {
	

        TextView tv;

        public void onCreate(Bundle savedInstanceState)
{

                        super.onCreate(savedInstanceState);
                        setContentView(R.layout.safety_tips);

                        
                        /*TextView t3 = (TextView) findViewById(R.id.text3);
                        t3.setText(
                            Html.fromHtml(
                                "<b>text3:</b>  Text with a " +
                                "<a href=\"http://blog.bicyclecoalition.org/2008/07/tips-for-locking-your-bicycle.html\">link</a> " +
                                "created in the Java source code using HTML."));
                        t3.setMovementMethod(LinkMovementMethod.getInstance());*/
                        
                        
                        tv = (TextView) findViewById(R.id.textView1);
                        tv.setText(
                                Html.fromHtml(
                        		"<font size=9>How to safely lock your " +
                        		"<a href=\"http://blog.bicyclecoalition.org/2008/07/tips-for-locking-your-bicycle.html\">bike</a> :" + 
                        		"<br> <br>  " +
                        		"1) Use a flat key U-Lock to secure the frame and rear wheel to the rack or other fixed object. <br> <br>" +
                        		"2) Use a cable lock to secure the front wheel. Alternatively you can use a U-Lock with a cable loop. <br> <br>" +
                        		"3) Two different types of locks require different tools to defeat and therefore make it a smaller target. <br> <br>" +
                        		"NHTSA Bicycle Safety Tips For Adults: A " +
                        		"<a href=\"http://www.bicyclecoalition.org/resources/presentations/bikevideos/safety\">video</a> " + 
                        		"produced by the League of American Bicyclists. <br> <br>" +
                        		"What Does It Mean To Share The Road?: A locally produced " +
                        		"<a href=\"http://www.bicyclecoalition.org/resources/presentations/bikevideos/dvrpc_safety\">bicycle safety video</a> " +
                        		"created by the Delaware Valley Regional Planning Commission. <br> <br>" +
                        		"Pennsylvania Bicycle Drivers Manual:  This " +
                        		"<a href=\"http://www.dot.state.pa.us/Internet/Bureaus/pdBikePed.nsf/infoAcknowledgements?OpenForm\">PENNDOT manual</a> " +
                        		"was adopted from the Rodale Press publication Street Smarts, by John S. Allen </font>"));
                                tv.setMovementMethod(LinkMovementMethod.getInstance());

        }
}

