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

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

public class IleSansFilApp extends Application {
	static final String CURRENTMODULE = "IleSansFil.Application";

	HotSpotManager hotSpotManager;
	private NetworkInfo[] netInfo;
	private int connectedNetId;
	private IleSansFil mainActivity=null;
	private MapTabView mapView=null;
	private boolean wifiStateThreadActive=false;
	private Thread wifiThread=null; 
	
	private boolean locationEnabled=false;

	private String versionName;

	private Drawable appIcon;

	public void init(boolean fromCache){
											 
		if(hotSpotManager==null) {
			hotSpotManager = new HotSpotManager(this, "https://auth.ilesansfil.org/hotspot_status.php?format=XML",HotSpotManager.statusFormat.xml);
			//hotSpotManager = new HotSpotManager(this, "http://blackhole.virtuelnet.com/public/hotspot.xml",HotSpotManager.statusFormat.xml);
			hotSpotManager.loadHotSpotsSax(fromCache);
		}
	}
	
	public HotSpotManager getHotSpotManager() {
		return hotSpotManager;
	}
	
	public void setMainActivity(IleSansFil mainActivity){
		this.mainActivity = mainActivity;
	}

	public IleSansFil getMainActivity(){
		return mainActivity;
	}
	
	public int getConnectedNetworkId() {
		return connectedNetId;
	}
	
	public boolean isNetworkAvailable()
	{
		State netState=State.DISCONNECTED;
		ConnectivityManager netManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		
		netInfo = netManager.getAllNetworkInfo();
		for(int i=0;i<netInfo.length;i++) {
			netState = netInfo[i].getState();
			if(netState == State.CONNECTED) {
				connectedNetId = i;
				return true;
			}
		}
		return (false);
	}
	
	public synchronized void stopWifiStatusCheck() {
		wifiStateThreadActive=false;
		if(wifiThread!=null) {
			try {
				wifiThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Log.i(CURRENTMODULE,"Could not Stop wifiThread");
			}
		} 
	}
		
	public void startWifiStatusCheck() {
		synchronized (this) { 
			if (wifiStateThreadActive==true) {
				return;
			} else {
				wifiStateThreadActive=true;
				wifiThread = new Thread() {
			        @Override
			        public void run() {
			            try {
			                int waited = 5000;
			        		WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			                while(wifiStateThreadActive) {
			                    sleep(1000);
			                    if(wifiStateThreadActive) {
			                        waited += 1000;
			                        if(waited >= 5000) {
			                        	waited=0;
//			                    		List<ScanResult> scanResult = wifiManager.getScanResults();
			                    		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			                    		Message msg = new Message();
			                    		Bundle data = new Bundle();
			                    		data.putBoolean("WifiEnable",wifiManager.isWifiEnabled());
			                    		if(wifiInfo.getSSID()!=null) {
			                    			data.putString("WifiCurrentBSSID", wifiInfo.getSSID());
			                    		} else {
			                    			data.putString("WifiCurrentBSSID", "");	
			                    		}
			                    		data.putString("WifiCurrentSupplicant", wifiInfo.getSupplicantState().toString());
			                    		msg.what=IleSansFil.MSG_SHOW_WIFI;
			                    		msg.setData(data);
			                    		getMainActivity().messageHandler.sendMessage(msg);
			                        }
			                    }
			                }
			            } catch(InterruptedException e) {
			                // do nothing
			            } finally {
			            	wifiStateThreadActive=false;
			            	Log.i(CURRENTMODULE,"Exiting WifiStateMonitoring Thread");
			            }
			        }
		        };
		        wifiThread.start();
			}
		}
	}
	

	public void setLocationEnabled(boolean value) {
		locationEnabled = value;
	}
	
	public boolean isLocationEnabled() {
		return(locationEnabled);
	}
	
	public void setMapView(MapTabView view) {
		mapView = view;
	}
	
	public MapTabView getMapView () {
		return mapView;
	}

	public String getVersion() {
		return versionName;
	}
	
	public Drawable getIcon() {
		return appIcon;
	}
	
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub		
		super.onCreate();
        Log.i(CURRENTMODULE,"Creating");
        PackageManager pm = getApplicationContext().getPackageManager();
        PackageInfo myInfo;
		try {
			myInfo = pm.getPackageInfo(getPackageName(), 0);
			this.versionName = myInfo.versionName;
			this.appIcon = pm.getApplicationIcon(getPackageName());
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
