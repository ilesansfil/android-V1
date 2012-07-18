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
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class IsfWebView extends Activity {
	private WebView browser;
	
	
	private class isfWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        browser.loadUrl(url);
	        return true;
	    }
	    
	}
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.isfwebview);
		browser=(WebView) findViewById(R.id.isfwebview);
		browser.setWebViewClient(new isfWebViewClient());
		browser.canGoBack();
		browser.getSettings().setJavaScriptEnabled(true);
		browser.loadUrl("http://www.ilesansfil.org/blogue/");
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && browser.canGoBack()) {
            browser.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
