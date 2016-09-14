package com.qianft.m.qian.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnKeyListener;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.qianft.m.qian.R;
import com.qianft.m.qian.activity.MainActivity.getHtmlObject;
import com.qianft.m.qian.common.Constant;
import com.qianft.m.qian.utils.ActivityCollector;
import com.qianft.m.qian.utils.LogUtil;
import com.umeng.message.PushAgent;
import com.umeng.socialize.PlatformConfig;

public class BaseActivity extends Activity{
	private String TAG = "BaseActvity";
	private boolean DEBUG = true;
	private WebView mWebView; 
	private String mAddress = Constant.Address;
	//public String mAddress = "file:///android_asset/html/index.html";
	//private String mAddress = "http://192.168.0.70:8088/Home/Index";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		LogUtil.d("BaseActivity", "BaseActivity:  " + getClass().getSimpleName());
		
		ActivityCollector.addActivity(this);
		PushAgent.getInstance(this).onAppStart();
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityCollector.removeActivity(this);
	}
	
}
