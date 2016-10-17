package com.qianft.m.qian.view;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.RenderPriority;

public class MyWebView extends WebView{


	public MyWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	public MyWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public MyWebView(Context context) {
		super(context);
		initView();
		
	}
	
	//生成WebViewClient
	protected MyWebViewClient generateMyWebViewClient() {
		return new MyWebViewClient(this);
	}
	
	//初始化webview
	private void initView() {
		WebSettings webSettings = this.getSettings();
		//开启JavaScript
		webSettings.setJavaScriptEnabled(true); 
		//设置字符编码
		webSettings.setDefaultTextEncodingName("utf-8");
		//调整到适合Webview大小
		webSettings.setLoadWithOverviewMode(true);   
		//webSettings.setAllowFileAccess(false);
		//Sets whether the WebView should enable support for the "viewport" HTML 
		webSettings.setUseWideViewPort(true);     
		//webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		//图片资源后加载
		webSettings.setBlockNetworkImage(true);  
		webSettings.setRenderPriority(RenderPriority.HIGH);
		//不保存密码
		webSettings.setSavePassword(false); 
		
		this.setWebViewClient(generateMyWebViewClient());
		
	}		
	
	
	
	
	
	

}
