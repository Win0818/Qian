package com.qianft.m.qian.activity;

import android.app.Activity;
import android.os.Bundle;

import com.qianft.m.qian.utils.ActivityCollector;
import com.qianft.m.qian.utils.LogUtil;
import com.umeng.message.PushAgent;

public class BaseActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.d("BaseActivity", getClass().getSimpleName());
		
		ActivityCollector.addActivity(this);
		PushAgent.getInstance(this).onAppStart();
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityCollector.removeActivity(this);
	}
	
	
	
}
