package com.qianft.m.qian.view;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyWebViewClient extends WebViewClient{

	private MyWebView webView;
	public MyWebViewClient (MyWebView webView) {
		this.webView = webView;
	}
	
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		
		view.loadUrl(url);
		return true;
	}
	
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		super.onPageStarted(view, url, favicon);
		
	}
	
	@Override
	public void onPageFinished(WebView view, String url) {
		super.onPageFinished(view, url);
	}
	@Override
	public void onReceivedError(WebView view, int errorCode,
			String description, String failingUrl) {
		super.onReceivedError(view, errorCode, description, failingUrl);
		
		
	}
	
}
