package com.qianft.m.qian.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qianft.m.qian.R;
import com.qianft.m.qian.common.Constant;
import com.qianft.m.qian.utils.MySharePreData;
import com.qianft.m.qian.utils.Util;
import com.umeng.analytics.MobclickAgent;

public class WelcomeActivity extends Activity{

	private RelativeLayout rootLayout;
	private TextView versionText;
	private ImageView mWelcomeImg;
	private static final int SLEEP_TIME = 3000;
	private boolean isFirst = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_welcome);
		
		rootLayout = (RelativeLayout)findViewById(R.id.welcome_root);
		mWelcomeImg = (ImageView) findViewById(R.id.welcome_image);
		if (Util.isNetWorkConnected(this)) {
			Glide.with(this).load(Constant.URL_IMAGE).asBitmap().into(mWelcomeImg);
		} else {
			mWelcomeImg.setBackgroundResource(R.drawable.welcome);
		}
		
		versionText = (TextView)findViewById(R.id.tv_version);
		versionText.setText("Version: " + getVersion());
		
		isFirst = MySharePreData.GetBooleanTrueData(this, Constant.NAVIGATION_SP_NAME, "is_first");
		//弹出动画
		AlphaAnimation aa = new AlphaAnimation(0.3f, 1.0f);
		aa.setDuration(1500);
		rootLayout.startAnimation(aa);
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		MySharePreData.GetBooleanData(this, Constant.NAVIGATION_SP_NAME, "is_first");
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				//第一次打开app打开引导界面
				if (isFirst) {
					startActivity(new Intent(WelcomeActivity.this, 
							NavigationActivity.class));
					finish();
				} else {
					try {
						Thread.sleep(SLEEP_TIME);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					startActivity(new Intent(WelcomeActivity.this, 
							MainActivity.class));
					finish();
				}
			}
		}).start();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	/**
	 * 获取当前应用程序的版本号
	 */
	private String getVersion() {
		String st = getResources().getString(R.string.Version_number_is_wrong);
		PackageManager pm = getPackageManager();
		try {
			PackageInfo packinfo = pm.getPackageInfo(getPackageName(), 0);
			String version = packinfo.versionName;
			return version;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return st;
		}
	}
}
