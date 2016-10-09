package com.qianft.m.qian.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.WebView;

import com.qianft.m.qian.common.Constant;
import com.qianft.m.qian.common.Global;
import com.qianft.m.qian.utils.ActivityCollector;
import com.qianft.m.qian.utils.LogUtil;
import com.umeng.message.PushAgent;

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
		registerScreenBroadcast();
	}
	
	//注册屏幕关闭和打开的广播
	public void registerScreenBroadcast() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mScreenBroadcast, filter);
	}
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityCollector.removeActivity(this);
		if (mScreenBroadcast != null) {
			unregisterReceiver(mScreenBroadcast);
		}
	}

	private BroadcastReceiver mScreenBroadcast = new BroadcastReceiver() {
    	public void onReceive(Context context, Intent intent) {
    		 String action = intent.getAction();  
             if(Intent.ACTION_SCREEN_ON.equals(action)){  
            	 //Screen_Off_Flag = true;
            	 Log.d("Wing", "ACTION_SCREEN_ON");
                // mScreenStateListener.onScreenOn();  
             }else if(Intent.ACTION_SCREEN_OFF.equals(action)){ 
            	 //if(!isAppOnForeground()){  
                     Log.d("wing", "ACTION_SCREEN_OFF  isAppOnForeground");  
                     if (Global.Screen_Off_Flag) {
                    	 Log.d("wing", "ACTION_SCREEN_OFF  isAppOnForeground-- onResume:-----2");  
                    	 MainActivity.isActive = false;  
                    	 Global.Screen_Off_Flag = false ;
                     }
                     
                 //}
            	 Log.d("Wing", "ACTION_SCREEN_OFF");
             }  
    	};
    };

}
