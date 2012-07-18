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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class MapTabView extends MapActivity implements LocationListener {

	private static final int LATITUDE=0;	/* Nord-sud */
	private static final int LONGITUDE=1;	/* Est-Ouest */
	private static final String CURRENTMODULE = "IleSansFil.MapTabView";
	private static final String[] S = { "Out of Service", "Temporarily Unavailable", "Available" };

	//  Real Montreal Limits
	private static final double[] LIMIT_TopLeftLongLat = { 45.712892,-74.058838};
	private static final double[] LIMIT_BottomRightLongLat = {45.34732,-73.425751};
	// Limit Including Repentigny for testing
	//private static final double[] LIMIT_TopLeftLongLat = { 45.78892,-74.020838};
	//private static final double[] LIMIT_BottomRightLongLat = {45.34732,-73.355751};
	private static final double[] MTL_DownTown_Location = {45.512474,-73.565140};
	private static final double LONGITUDE_DELTA = 0.0002; // 0.0002 == 15.72 meters
	private static final double LATITUDE_DELTA = 0.0001;  // 0.0001 == 11.11 meters
		
	private double[] currentLocation=MTL_DownTown_Location;
		
//	private int pinDisplayedCount=0;
	private int lastZoomLevel=0;
	
//	private static final int ICON_HEIGHT=34, ICON_WIDTH=21;

	private MapItemizedOverlay mapOverlay;
	private LocationManager locationManager;
	private String bestProvider;

//	private TextView locationInfoView;
	private IleSansFilApp mainApp;
	
	private MapView mapView;
	
	class PinDefinition {
		public GeoPoint p;
		public String name;
		public String info; 
	}
	
	/* class MapOverlay extends com.google.android.maps.Overlay
	    {
		
			GeoPoint p;
		 	
		 	public GeoPoint getP() {
				return p;
			}

			int pinImageId;
		 	int pinXOffset, pinYOffset;
		 	Bitmap pinBitmap; 
			        
		 	public MapOverlay(double latitude, double longitude, int imageId, int xOffset, int yOffset )
		 	{
		 		super();
		 		p = new GeoPoint((int) (latitude*1E6),(int)(longitude*1E6));
		 		pinImageId = imageId;
		 		pinXOffset = xOffset;
		 		pinYOffset = yOffset;
		 		pinBitmap= BitmapFactory.decodeResource(getResources(),pinImageId );		 		
		 	}
		 	
		 	
		 	@Override
	        public boolean draw(
	        		Canvas canvas, MapView mapView, 
	        		boolean shadow, long when) 
	        {
	            super.draw(canvas, mapView, shadow);                   
	 
	            //---add the marker---
	            if(lastZoomLevel!=mapView.getZoomLevel()) {
	            	pinDisplayedCount=0;
	            }
	            if(pinDisplayedCount < 10) {
		            //---translate the GeoPoint to screen pixels---
	            	if (p.getLatitudeE6()>(mapView.getMapCenter().getLatitudeE6()+mapView.getLatitudeSpan()/2)) return false;
	            	if (p.getLatitudeE6()<(mapView.getMapCenter().getLatitudeE6()-mapView.getLatitudeSpan()/2)) return false;
	            	if (p.getLongitudeE6()>(mapView.getMapCenter().getLongitudeE6()+mapView.getLongitudeSpan()/2)) return false;
	            	if (p.getLongitudeE6()<(mapView.getMapCenter().getLongitudeE6()-mapView.getLongitudeSpan()/2)) return false;
		            Point screenPts = new Point();
		            mapView.getProjection().toPixels(p, screenPts);
		            canvas.drawBitmap(pinBitmap, screenPts.x+pinXOffset, screenPts.y+pinYOffset, null);         
		            return true;
	            }
	            
	            return false;
	        }

			 @Override
			 public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			     
			 	 // Next test whether a new popup should be displayed
				boolean isRemovePriorPopup=false;
				
		 	    if(hitTest(event)) 
		 	    {
		 	    	
		 	    	if ( isRemovePriorPopup) {
			 	        mapView.invalidate();
			 	    }
			 	    // Lastly return true if we handled this onTap()
		 	    	return true;
		 	    }
		 	    return false;
		 	}


		    private boolean hitTest(MotionEvent event) {

		    	Point screenPts = new Point();
	            RectF hitTestRecr = new RectF();
		        
	            // As above, translate MapLocation lat/long to screen coordinates
		        mapView.getProjection().toPixels(p, screenPts);
	            
	            // Use this information to create a hit testing Rectangle to represent the size
	            // of our locations icon at the correct location on the screen.
	            // As we want the base of our balloon icon to be at the exact location of
	            // our map location, we set our Rectangles location so the bottom-middle of
	            // our icon is at the screen coordinates of our map location (shown above).
	            hitTestRecr.set(-ICON_WIDTH/2,-ICON_HEIGHT,ICON_WIDTH/2,0);

	            // Next, offset the Rectangle to location of our locations icon on the screen.
	            hitTestRecr.offset(screenPts.x, screenPts.y);

	            // Finally test for match between hit Rectangle and location clicked by the user.
	            // If a hit occurred, then we stop processing and return the result;
	            if (hitTestRecr.contains(event.getX(),event.getY())) {
	                return true;
	            }
		        return false;
		    }
		 	
	    }
*/
	
	private class MapItemizedOverlay extends ItemizedOverlay<OverlayItem> {

		private final Drawable[] ds = {
				getResources().getDrawable(R.drawable.pin_w_green),
				getResources().getDrawable(R.drawable.pin_w_yellow),
				getResources().getDrawable(R.drawable.pin_w_red)
				};

		private ArrayList<OverlayItem> items;
		private Drawable marker=null;
		private boolean enabled=false;
		//private int overlayStatus;
	
		private class HotSpotOverlayItem extends OverlayItem {
				
			final static int UP=0;
			final static int MAYBE=1;
			final static int DOWN=2;
			
			private int status=UP;
			private HotSpot hs;
			
			public HotSpotOverlayItem(GeoPoint point, HotSpot hs) {
				super(point, hs.getName(), "");
				this.hs = hs;
			}
		
			public HotSpotOverlayItem setStatus(int status) {
				this.status=status;
				return this;
			}
			
			public int getStatus() {
				return status;
			}
			
			/*@Override
			public Drawable getMarker(int bitState) {
				return ds[status];
				//return mMarker;
			}*/
		}
		
		public MapItemizedOverlay(Drawable marker) {
			super(marker);
			
			this.marker = marker;
			//this.overlayStatus = status;
			Log.i(CURRENTMODULE+":MapItemizedOverlay","Init");
			items = new ArrayList<OverlayItem>();
			boundCenterBottom(marker);
			boundCenterBottom(ds[0]);
			boundCenterBottom(ds[1]);
			boundCenterBottom(ds[2]);
		
		}
/*			
			items.add(new OverlayItem(
					new GeoPoint((int) (45.512*1E6),(int)(-73.565*1E6)),
					"UN", "United Nations"));
			items.add(new OverlayItem(
				new GeoPoint((int) (45.522*1E6),(int)(-73.575*1E6)),
				"Lincoln Center", "Home of Jazz at Lincoln Center"));
			items.add(new OverlayItem(
				new GeoPoint((int) (45.502*1E6),(int)(-73.545*1E6)),
				"Carnegie Hall", "Where you go with practice, practice, practice"));
*/				 
		
		public void addHotSpots(){
			
			IleSansFilApp app = (IleSansFilApp) getApplication();
			HotSpotManager hsMgr = app.getHotSpotManager();
			ArrayList<HotSpot> hotSpots = hsMgr.getHotSpots();
			for(Iterator<HotSpot> i=hotSpots.iterator();i.hasNext();) {
				HotSpot hotSpot = (HotSpot) i.next();
/*				boolean selected;
				if(overlayStatus>0) {
					selected = (hotSpot.getGlobalStatus()>=overlayStatus);
				} else {
					selected = (hotSpot.getGlobalStatus()<=-overlayStatus); 
				}
				if(selected && hotSpot.getGisCenterLatLong(0)!=0.0) {*/
				if(hotSpot.getGisCenterLatLong(0)!=0.0) {
					HotSpotOverlayItem item = new HotSpotOverlayItem(
							new GeoPoint(
									(int)(hotSpot.getGisCenterLatLong(0)*1E6),
									(int)(hotSpot.getGisCenterLatLong(1)*1E6)),	hotSpot);
					if(hotSpot.getGlobalStatus()==0) { 
						//item.setStatus(HotSpotOverlayItem.DOWN);
						item.setMarker(ds[HotSpotOverlayItem.DOWN]);
					}
					else if(hotSpot.getGlobalStatus()<100) {
						//item.setStatus(HotSpotOverlayItem.MAYBE);
						item.setMarker(ds[HotSpotOverlayItem.MAYBE]);
					}
					items.add(item);
				}
			}
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void enableOverlay() {
			enabled=true;
			populateOverlay();
		}
		
		public void disableOverlay() {
			enabled=false;
			populateOverlay();
		}
		
		public void populateOverlay(){
			setLastFocusedIndex(-1);
			populate();
		}
				
		@Override
		protected OverlayItem createItem(int i) {
			return(items.get(i));			
		}
		 
		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);		 
		}
		
		@Override
		public int size() {
			if(enabled)	return(items.size());
			else return 0;
		}
		
		@Override
		protected boolean onTap(int i) {
				
			Log.i(CURRENTMODULE+":MapItemizedOverlay","onTap:"+i);
			HotSpotOverlayItem item = (HotSpotOverlayItem) getItem(i);
			  AlertDialog.Builder builder = new AlertDialog.Builder(mainApp.getMainActivity());
			  builder.setTitle(item.hs.getName())  
			  .setMessage(item.hs.getCivicNumber()+" "+item.hs.getStreetAddress()+"\n\n"
					  +item.hs.getGisCenterLatLong(0)+"/"+item.hs.getGisCenterLatLong(1))   
			  .setInverseBackgroundForced(true)
			  .setCancelable(true);
			  AlertDialog alert = builder.create();
			  alert.show();
			
			Message msg = new Message();
			msg.obj = item.hs;
			msg.what = IleSansFil.MSG_SHOW_HOTSPOT;
  			mainApp.getMainActivity().messageHandler.sendMessage(msg);
			return true;			
		}		 		
	}
	
	 @Override
	 protected boolean isRouteDisplayed() {
	   return false;
	 }
	 	 	 
	 private void centerMap(double latitude, double longitude)
	 {
		 MapController mc =mapView.getController();

		 	GeoPoint p = new GeoPoint(
	            (int) (latitude * 1E6), 
	            (int) (longitude * 1E6));
	 
	        mc.animateTo(p);
	        lastZoomLevel=16;
	        mc.setZoom(lastZoomLevel); 
	        mapView.invalidate(); 
	 }
	 
	 private void addMarkers()
	 {					
		//---Add a location marker---
			
	        //MapOverlay mapOverlay = new MapOverlay(45.512474, -73.565140,R.drawable.pin_w_green,-10,-34);
	        
		// ---Add ItemizedOverlay ---
		 	List<Overlay> listOfOverlays = mapView.getOverlays();
	        listOfOverlays.clear();
	        mapOverlay =new MapItemizedOverlay(getResources().getDrawable(R.drawable.pin_w_green));
	        mapOverlay.addHotSpots();
		 	listOfOverlays.add(mapOverlay);
		 	mapOverlay.enableOverlay();
		 	//mapOverlay =new MapItemizedOverlay(getResources().getDrawable(R.drawable.pin_w_red),0);
		 	//listOfOverlays.add(mapOverlay);
		 	
	        mapView.invalidate();		 
	 }
	
	 private void logLocationInfo(String text) {
		 Log.i(CURRENTMODULE, text);
	 }

	public boolean togglePins() {
		if(mapOverlay.isEnabled()) mapOverlay.disableOverlay();
		else mapOverlay.enableOverlay();
		mapView.invalidate();
		return mapOverlay.isEnabled();
	}
	 
	public void initLocationManagement() {
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			// List all providers:
		List<String> providers = locationManager.getAllProviders();
		for (String provider : providers) {
			printProvider(provider);
		}
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		bestProvider = locationManager.getBestProvider(criteria, true);
		logLocationInfo("\n\nBEST Provider:\n");
		printProvider(bestProvider);
		//logLocationInfo("\n\nLocations (starting with last known):");
		//Location location = locationManager.getLastKnownLocation(bestProvider);
		//printLocation(location);
		setLocationDetection(true);
	}

	
	/** Register for the updates when Activity is in foreground */
	@Override
	protected void onResume() {
		super.onResume();
//		if(mainApp.isLocationEnabled())	locationManager.requestLocationUpdates(bestProvider, 20000, 1, this);
	}
		
	/** Stop the updates when Activity is paused */
	@Override
	protected void onPause() {
		super.onPause();
//		if(mainApp.isLocationEnabled()) locationManager.removeUpdates(this);
	}

	public void onLocationChanged(Location location) {
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		if(latitude>LIMIT_TopLeftLongLat[LATITUDE] ||
			latitude<LIMIT_BottomRightLongLat[LATITUDE] ||
			longitude<LIMIT_TopLeftLongLat[LONGITUDE] ||
			longitude>LIMIT_BottomRightLongLat[LONGITUDE]) {
			// Not in Montreal Turn Off GPS
			setLocationDetection(false);
		} else {
			if( Math.abs(longitude-currentLocation[LONGITUDE])>LONGITUDE_DELTA 
				|| Math.abs(latitude-currentLocation[LATITUDE])>LATITUDE_DELTA) {
				currentLocation[LATITUDE]=latitude;
				currentLocation[LONGITUDE]=longitude;
				centerMap(latitude, longitude);
			}
		}
		printLocation(location);
	}

	public void onProviderDisabled(String provider) {
		// let okProvider be bestProvider
		// re-register for updates
		
		logLocationInfo("\n\nProvider Disabled: " + provider);
	}
		
	public void onProviderEnabled(String provider) {
		// is provider better than bestProvider?
	// is yes, bestProvider = provider
		logLocationInfo("\n\nProvider Enabled: " + provider);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		logLocationInfo("\n\nProvider Status Changed: " + provider + ", Status="
				+ S[status] + ", Extras=" + extras);
	}

	private void printProvider(String provider) {
		LocationProvider info = locationManager.getProvider(provider);
		logLocationInfo(info.toString() + "\n\n");
	}

	private void printLocation(Location location) {
		if (location == null)
			logLocationInfo("\nLocation[unknown]\n\n");
		else {
			logLocationInfo("\n\n" + location.toString());
			Message msg = new Message();
			msg.what = IleSansFil.MSG_SHOW_LOCATION;
			msg.arg1 = (int) (location.getLatitude()*1E6);
			msg.arg2 = (int) (location.getLongitude()*1E6);
			mainApp.getMainActivity().messageHandler.sendMessage(msg);
		}
	}
	  
	public void setLocationDetection(boolean value) {
		mainApp.setLocationEnabled(value);
		if(value==true) {
    		locationManager.requestLocationUpdates(bestProvider, 20000, 1, this);
		} else {
    		locationManager.removeUpdates(this);
		}
	}
	
	public double[] getCurrentLocation() {
		double[] d={0.0,0.0};
		if(locationManager!=null) {
			Location l = locationManager.getLastKnownLocation(bestProvider);
			d[0]= l.getLatitude();
			d[1]= l.getLongitude();
		} 
		return(d);
	}
	
	@Override
	 protected void onCreate(Bundle icicle) {
	    super.onCreate(icicle);
	     
	    mainApp = (IleSansFilApp) getApplication();
	    mainApp.setMapView(this);
	    setContentView(R.layout.maptabview);
	    mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.setClickable(true);

        // TODO Get Montreal Coord from HotSpotManager
        centerMap(currentLocation[LATITUDE],currentLocation[LONGITUDE]);	//DownTown Montreal      
        addMarkers();
	 }

	 
}