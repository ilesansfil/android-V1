/*******************************************************************************
 * Copyright (c) 2010 Octade Technologies  
 * 
 * This file is part of the Android IleSansFil Application project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  
 *******************************************************************************/
package com.octade.droid.ilesansfil;

//import android.app.Activity;
import java.util.Date;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ToggleButton;
 
public class IleSansFil extends TabActivity {
	
	static final String CURRENTMODULE = "IleSansFil.TabActivity";
	//static final String VERSION = "0.4";

	static final int MENU_LOCATION=1;
	static final int MENU_SETTINGS=2;
	static final int MENU_ABOUT=3;
	static final int MENU_TOGGLE_PIN=4;
	
	public static final int MSG_SHOW_HOTSPOT=1;
	public static final int MSG_SHOW_WIFI=2;
	public static final int MSG_SHOW_LOCATION=3;	
	private TabHost mTabHost;
	private IleSansFilApp mainApp;
	
	private OnClickListener wifiEnableClickListener = new OnClickListener () {

		@Override
		public void onClick(View arg0) {
    		WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    		ToggleButton button = (ToggleButton) arg0;
    		wifiManager.setWifiEnabled(button.isChecked());
		}
	};
	
	private OnClickListener wifiSettingClickListener = new OnClickListener () {
		@Override
		public void onClick(View arg0) {
			startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
		}
	};
		
	private OnClickListener findHotSpotClickListener = new OnClickListener () {
		@Override
		public void onClick(View arg0) {
			HotSpot closestHS = mainApp.hotSpotManager.findClosestHotSpot(
					mainApp.getMapView().getCurrentLocation());
			TextView v=(TextView) findViewById(R.id.TextView23);
			v.setText(closestHS.getName()+"  ("+closestHS.getGlobalStatus()+")\n"
					+closestHS.getCivicNumber()+" "+closestHS.getStreetAddress()
					+closestHS.getWebSiteUrl()+"\n"
					+closestHS.getContactPhoneNumber()+"\n\n"
					
					+closestHS.getMassTransitInfo()+"\n"
					+"Description:\n"+closestHS.getDescription().trim()
			);
		}
	};

	
	// Instantiating the Handler associated with the main thread.
    public Handler messageHandler = new Handler() {
    	TextView v;
	    @Override
	    public void handleMessage(Message msg) {
	    	  
	      switch(msg.what) {
	    	  
	      case MSG_SHOW_HOTSPOT:
	    	  HotSpot hs = (HotSpot) msg.obj;
	    	  v = (TextView) findViewById(R.id.TextView23);
	    	  v.setText(
	    			  hs.getName()+"\n\n"
	    			  +hs.getCivicNumber()+" "+hs.getStreetAddress()+"\n\n"
	    			  +hs.getDescription().trim());
	    	  break;
	    	  /*try {
    			  AlertDialog.Builder builder = new AlertDialog.Builder(mainApp.getMainActivity());
    			  builder.setTitle(hs.getName())  //item.getTitle())
    			  .setMessage(hs.getDescription())    //item.getSnippet())
    			  .setInverseBackgroundForced(true)
    			  .setCancelable(true);
    			  AlertDialog alert = builder.create();
    			  alert.show();
    			  return;
    		  } catch (Exception e) {
    			  Log.i (CURRENTMODULE,"Exception occured while displaying HotSpot Dialog");
    		  }*/
          
    	  case MSG_SHOW_WIFI:
    		  Bundle data =msg.getData();
    		  if(data!=null) {
   				  ToggleButton button = (ToggleButton) findViewById(R.id.ToggleButton01);
   				  boolean iswifiEnabled = data.getBoolean("WifiEnable");
   				  button.setChecked(iswifiEnabled);
   				  v = (TextView) findViewById(R.id.infoText11);
    			  v.setText(getResources().getString(R.string.infoSSID)+data.getString("WifiCurrentBSSID"));
    			  v = (TextView) findViewById(R.id.infoText12);
    			  v.setText(getResources().getString(R.string.infoSupplicant)+data.getString("WifiCurrentSupplicant"));
    		  }
    		  break;
    		  
	      case MSG_SHOW_LOCATION:
	    	  double dLat = msg.arg1 / 1E6;
	    	  double dLong = msg.arg2 / 1E6;
	    	  TextView vLat = (TextView) findViewById(R.id.TextViewLat);
	    	  TextView vLong = (TextView) findViewById(R.id.TextViewLong);
	    	  vLat.setText("Lat: "+dLat);
	    	  vLong.setText("Long: "+dLong);
    	  }
      }
  };
	  
	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
		
	    menu.add(0, MENU_LOCATION, 0, getResources().getString(R.string.menuGpsOn)).setCheckable(true).setChecked(true); 
//	    menu.add(0, MENU_SETTINGS, 1, "Settings");
	    menu.add(0, MENU_ABOUT, 2, getResources().getString(R.string.menuAbout));
	    menu.add(0, MENU_TOGGLE_PIN, 3, getResources().getString(R.string.menuPinOn)).setCheckable(true).setChecked(true);
	    return true;
	}

	private void about()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage("\n"+
				"Android Application"+"\n"+
				"version: "+mainApp.getVersion()+"\n\n"+
				"Octade Technologies inc.\n"+
				"http://octade.virtuelnet.com\n")
			   .setInverseBackgroundForced(true)
			   .setTitle("Ile sans fil")
			   .setIcon(mainApp.getIcon())
		       .setCancelable(true);
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case MENU_LOCATION:
	        if(item.isChecked()==false) {
	        	item.setChecked(true);
	        	item.setTitle(getResources().getString(R.string.menuGpsOn));
	        	mainApp.getMapView().setLocationDetection(true);
	        	
	        } else {
	        	item.setChecked(false);
	        	item.setTitle(getResources().getString(R.string.menuGpsOff));
	        	mainApp.getMapView().setLocationDetection(false);
	        }
	        return true;
	    case MENU_SETTINGS:
	        
	        return true;
	    case MENU_ABOUT:
	        about();
	        return true;
		case MENU_TOGGLE_PIN:
			if(mainApp.getMapView().togglePins()==true) {
		    	item.setChecked(true);
				item.setTitle(getResources().getString(R.string.menuPinOn));
			} else {
	        	item.setChecked(false);
				item.setTitle(getResources().getString(R.string.menuPinOff));
			}
			
			return true;
	    }
        return false;
	}
	
	private void setupTabs() {
		
        mTabHost = getTabHost();
        Context ctx = this.getApplicationContext();

        mTabHost.addTab(mTabHost.newTabSpec("tab_map")
        		.setIndicator("Map",getResources().getDrawable(R.drawable.maptab))
        		.setContent(new Intent(ctx, MapTabView.class))
        		);
        mTabHost.addTab(mTabHost.newTabSpec("tab_list")
        		.setIndicator("News",getResources().getDrawable(R.drawable.newstab))
//        		.setContent(R.id.Tab02)
        		.setContent(new Intent(ctx,IsfWebView.class))
        		);
        mTabHost.addTab(mTabHost.newTabSpec("tab_more")
        		.setIndicator("info",getResources().getDrawable(R.drawable.infotab))
        		.setContent(R.id.Tab03)
        		);
        mTabHost.setCurrentTab(0);
        mainApp.getMapView().initLocationManagement();       
	}

	// Activity becomes visible 
	@Override
    public void onStart() {
		super.onStart();
		Log.i(CURRENTMODULE,"Start:Visible");
	}
	
	// Activity becomes Invisible 
	@Override
    public void onStop() {
		super.onStop();
		Log.i(CURRENTMODULE,"Stop:InVisible");
	}

	// Activity becomes Foreground	
	@Override
    public void onResume() {
		super.onResume();
		mainApp.startWifiStatusCheck();
		if(mainApp.isLocationEnabled()) mainApp.getMapView().setLocationDetection(true);
		Log.i(CURRENTMODULE,"Resume:ForeGround");
	}

	// Activity becomes Background	
	@Override
    public void onPause() {
		super.onPause();
		mainApp.stopWifiStatusCheck();
		if(mainApp.isLocationEnabled()) mainApp.getMapView().setLocationDetection(false);
		Log.i(CURRENTMODULE,"Pause:Background");
	}
	
	// Activity Removed from memory
	@Override
    public void onDestroy() {
		super.onDestroy();
		Log.i(CURRENTMODULE,"Destroyed");
	}

	// After Restarting from STOPPED (onStart will follow)
	@Override
	public void onRestart (){
		super.onRestart();
		Log.i(CURRENTMODULE,"Restarting");
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		Date d=new Date();
		outState.putString("DDD", d.toLocaleString());
		super.onSaveInstanceState(outState);
		Log.i(CURRENTMODULE,"onSaveInstanceState Called");
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle inState) {
        super.onCreate(inState);
        if(inState!=null) {
        		Log.i(CURRENTMODULE,"Creating From Bundle :"+inState.getString("DDD"));
        		try {
					Date d = DateUtils.parseDate(inState.getString("DDD"));
					Date now=new Date();
					if((now.getTime()-d.getTime()) > (1000*3600*24)) {
						Log.i(CURRENTMODULE,"More than 1 day on Sleep");
					}
				} catch (DateParseException e) {
					// TODO Auto-generated catch block
					Log.i(CURRENTMODULE,"State date cannot be read");
				}
        } else {
    		Log.i(CURRENTMODULE,"Creating From Scratch:");
        }
        mainApp = (IleSansFilApp) getApplication();
        setContentView(R.layout.main);
        setupTabs();
        ToggleButton button1 = (ToggleButton) findViewById(R.id.ToggleButton01);
        button1.setOnClickListener(wifiEnableClickListener);
        Button button2 = (Button) findViewById(R.id.Button01);
        button2.setOnClickListener(wifiSettingClickListener);
        Button button3 = (Button) findViewById(R.id.hotSpotLocateButton);
        button3.setOnClickListener(findHotSpotClickListener);
        mainApp.setMainActivity(this);
    }
    
}