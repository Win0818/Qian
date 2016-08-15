package com.qianft.m.qian.activity;

import android.app.Activity;
import android.os.Bundle;

import com.qianft.m.qian.utils.ActivityCollector;
import com.qianft.m.qian.utils.LogUtil;

public class BaseActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.d("BaseActivity", "BaseActivity:  " + getClass().getSimpleName());
		
		ActivityCollector.addActivity(this);
		//友盟推送
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityCollector.removeActivity(this);
	}
	
	
	
}
