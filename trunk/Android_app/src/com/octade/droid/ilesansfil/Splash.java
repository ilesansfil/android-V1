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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class Splash extends Activity {

	static final String CURRENTMODULE = "IleSansFil.Splash";
	protected static final int MSG_PROGRESS = 1;
	protected boolean _active = true;
	protected int _splashTime = 500; // time to display the splash screen in ms
	private int direction=1;

	//Handler for displaying the progress bar
	public Handler h = new Handler() {
		private int progressCounter=0;
	    @Override
	    public void handleMessage(Message msg) {
	      switch(msg.what) {
	      	case MSG_PROGRESS:
	    	  ImageView v = (ImageView) findViewById(R.id.SplashLoading);
	    	  v.offsetLeftAndRight(10*direction);
	    	  progressCounter+=direction;
    		  if(progressCounter>15||progressCounter<0) direction=-direction;
    		  if(_active) {
    			  Message m = new Message();
    			  m.what=MSG_PROGRESS;
    			  h.sendMessageDelayed(m,1500);
    		  }
    		  break;
	      }
	    }
    };
    
    // thread for displaying the SplashScreen
    Thread splashTread = new Thread() {
        @Override
        public void run() {
            try {
                int waited = 0;
                IleSansFilApp mainApp = (IleSansFilApp) getApplication(); 
                mainApp.init(false);
                while(_active && (waited < _splashTime)) {
                    sleep(100);
                    if(_active) {
                        waited += 100;
                    }
                }
            } catch(InterruptedException e) {
                // do nothing
            } finally {
                Log.i(CURRENTMODULE,"Starting Main Activity");
                startActivity(new Intent(getApplicationContext(),IleSansFil.class));
                finish();
            }
        }
    };
    


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.splash);
        Log.i(CURRENTMODULE,"Creating");
	    splashTread.start();
	    //Message msg = new Message();
	    //msg.what=MSG_PROGRESS;
	    //h.sendMessageDelayed(msg,1500);
	    ImageView v = (ImageView) findViewById(R.id.SplashLoading);
	    TranslateAnimation anim=new TranslateAnimation(-150, 0, 0, 0);
        anim.setDuration(1000);
        anim.setRepeatCount(Animation.INFINITE);
		anim.setRepeatMode(Animation.REVERSE);
        v.setAnimation(anim); 
        anim.start();	    
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    if (event.getAction() == MotionEvent.ACTION_DOWN) {
	        //_active = false;
	    }
	    return true;
	}
}
