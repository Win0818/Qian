package com.qianft.m.qian.activity;

import com.qianft.m.qian.R;
import com.qianft.m.qian.common.Constant;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TestActivity extends BaseActivity{

	private WebView mWebView;
	private boolean DEBUG = true;
	private String mAddress = Constant.Address;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		mWebView = (WebView) findViewById(R.id.play_video);
		
		setWebView();
		
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private void setWebView() {
		try {
			WebSettings webSettings = mWebView.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setDefaultTextEncodingName("utf-8");
			//mWebView.addJavascriptInterface(new HtmlObject(this), "jsVideoPlayObj");
			mWebView.loadUrl(mAddress);
			mWebView.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if (DEBUG)
						Log.e("Wing", "..shouldOverrideUrlLoading.. url=" + url);
					view.loadUrl(url);
					return true;
				}
			});
			
			mWebView.setWebChromeClient(new WebChromeClient() {

				@Override
				public boolean onJsAlert(WebView view, String url,
						String message, JsResult result) {
					return super.onJsAlert(view, url, message, result);
				}

				@Override
				public boolean onJsConfirm(WebView view, String url,
						String message, JsResult result) {
					return super.onJsConfirm(view, url, message, result);
				}

			});
		} catch (Exception e) {
			return;
		}
	}
}
