package com.qianft.m.qian.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qianft.m.qian.BaseApplication;
import com.qianft.m.qian.R;
import com.qianft.m.qian.common.Constant;
import com.qianft.m.qian.utils.MySharePreData;
import com.qianft.m.qian.utils.Util;
import com.umeng.analytics.MobclickAgent;

public class WelcomeActivity extends BaseActivity{

	private String TAG = this.getClass().getSimpleName();
	private RelativeLayout rootLayout;
	private TextView versionText;
	private ImageView mWelcomeImg;
	private static final int SLEEP_TIME = 3000;
	private boolean isFirst = true;
	private String isLogin ;
	private WebView mWebView;
	//private String mAddress = "file:///android_asset/html/index.html";
	private String mAddress = "http://192.168.0.88:8011/";
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				testEvaluateJavascript_1(mWebView);
			/*	if (BaseApplication.getInstance().getLockPatternUtils().savedPatternExists() 
						&& isLogin != null && isLogin.equals("true")) {
					startActivity(new Intent(WelcomeActivity.this, 
							UnlockGesturePasswordActivity.class));
				}else {
					startActivity(new Intent(WelcomeActivity.this, 
							MainActivity.class));
				}
				finish();*/
				break;
			case 2:
				
				break;

			default:
				break;
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
		mWebView = (WebView) findViewById(R.id.webview_welcome);
		webViewSetting();
		versionText = (TextView)findViewById(R.id.tv_version);
		versionText.setText("Version: " + getVersion());
		
		isFirst = MySharePreData.GetBooleanTrueData(this, Constant.NAVIGATION_SP_NAME, "is_first");
		//弹出动画
		AlphaAnimation aa = new AlphaAnimation(0.3f, 1.0f);
		aa.setDuration(1500);
		rootLayout.startAnimation(aa);
	}
	
	public void button(View view) {
		testEvaluateJavascript_1(mWebView);
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private void webViewSetting() {
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setDefaultTextEncodingName("utf-8");
		mWebView.addJavascriptInterface(/*getHtmlObject()*/new HtmlObject(), "jsObj");
		mWebView.loadUrl(mAddress);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.e("Wing", "..shouldOverrideUrlLoading.. url=" + url);
				view.loadUrl(url);
				return true;
			}
		});
	}
	private class HtmlObject{
		
		@JavascriptInterface
		private void init() {
			
		}
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
					
					mHandler.sendEmptyMessage(1);
					
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
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//testEvaluateJavascript(mWebView);
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
	/**
	 * 调用JS函数
	 * @param webview
	 */
	 @SuppressLint("NewApi") 
	private void testEvaluateJavascript_1(WebView webview) {
	    	webview.evaluateJavascript("appCheckLogin()", new ValueCallback<String>() {
				@Override
				public void onReceiveValue(String value) {
					isLogin = value;
					Log.i(TAG, "onReceiveValue value=   " + isLogin + "----+++++++" + value);
					if (BaseApplication.getInstance().getLockPatternUtils().savedPatternExists() 
							&& isLogin != null && isLogin.equals("\"ok\"")) {
						startActivity(new Intent(WelcomeActivity.this, 
								UnlockGesturePasswordActivity.class));
					}else {
						startActivity(new Intent(WelcomeActivity.this, 
								MainActivity.class));
					}
					finish();
				}
			});
	    }
}
