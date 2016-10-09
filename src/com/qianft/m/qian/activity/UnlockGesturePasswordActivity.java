package com.qianft.m.qian.activity;

import java.util.List;

import org.greenrobot.eventbus.EventBus;

import com.qianft.m.qian.BaseApplication;
import com.qianft.m.qian.R;
import com.qianft.m.qian.common.Constant;
import com.qianft.m.qian.common.Global;
import com.qianft.m.qian.view.LockPatternUtils;
import com.qianft.m.qian.view.LockPatternView;
import com.qianft.m.qian.view.LockPatternView.Cell;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;


public class UnlockGesturePasswordActivity extends Activity implements OnClickListener{
	
	private String TAG = this.getClass().getSimpleName();
	private LockPatternView mLockPatternView;
	private int mFailedPatternAttemptsSinceLastTimeout = 0;
	private CountDownTimer mCountdownTimer = null;
	private Handler mHandler = new Handler();
	private TextView mHeadTextView;
	private Animation mShakeAnim;
	private TextView mForgetPassword;
	private WebView mWebView;
	private Toast mToast;
	private String mAddress = Constant.Address;
	//private String mAddress = "http://192.168.0.88:8011/";
	private void showToast(CharSequence message) {
		if (null == mToast) {
			mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
			//mToast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 500);
		} else {
			mToast.setText(message);
		}
		mToast.show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gesturepassword_unlock);
		mWebView = (WebView) findViewById(R.id.webview_unlock);
		webViewSetting();
		mLockPatternView = (LockPatternView) this
				.findViewById(R.id.gesturepwd_unlock_lockview);
		mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
		//mLockPatternView.setTactileFeedbackEnabled(true);
		mLockPatternView.setTactileFeedbackEnabled(false);
		mHeadTextView = (TextView) findViewById(R.id.gesturepwd_unlock_text);
		mForgetPassword = (TextView) findViewById(R.id.gesturepwd_unlock_forget);
		mForgetPassword.setOnClickListener(this);
		mShakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake_x);
	}

	@Override
	protected void onResume() {
		super.onResume();

		/*if (!App.getInstance().getLockPatternUtils().savedPatternExists()) {
			startActivity(new Intent(this, GuideGesturePasswordActivity.class));
			finish();
		}*/
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
	protected void onDestroy() {
		super.onDestroy();
		
		if (mCountdownTimer != null)
			mCountdownTimer.cancel();
		
	}
	
	private Runnable mClearPatternRunnable = new Runnable() {
		public void run() {
			mLockPatternView.clearPattern();
		}
	};
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.gesturepwd_unlock_forget: //忘记密码
			// Toast.makeText(UnlockGesturePasswordActivity.this, "forget password", Toast.LENGTH_LONG).show();
			 Intent intent = new Intent(UnlockGesturePasswordActivity.this, MainActivity.class);
			 intent.setAction("com.qianft.m.qian.login");
			 intent.putExtra("login_url", "http://m.qianft.com/UserLogin");
			 //intent.putExtra("login_url", "http://192.168.0.88:8011/UserLogin");
			 mWebView.loadUrl("javascript:" + "appLoginOut()");
			 //startActivity(intent);//跳转
			//退出登录刷新
				EventBus.getDefault().post("refresh_url");
			 BaseApplication.getInstance().
			 	getLockPatternUtils().clearLock();
			 finish(); 
			break;
		default:
			break;
		}
	}
	
	protected LockPatternView.OnPatternListener mChooseNewLockPatternListener = new LockPatternView.OnPatternListener() {

		public void onPatternStart() {
			mLockPatternView.removeCallbacks(mClearPatternRunnable);
			patternInProgress();
		}

		public void onPatternCleared() {
			mLockPatternView.removeCallbacks(mClearPatternRunnable);
		}
		public void onPatternDetected(List<LockPatternView.Cell> pattern) {
			if (pattern == null)
				return;
			if (BaseApplication.getInstance().getLockPatternUtils().checkPattern(pattern)) {
				mLockPatternView
						.setDisplayMode(LockPatternView.DisplayMode.Correct);
				Intent intent = new Intent(UnlockGesturePasswordActivity.this,
						MainActivity.class);
				// 打开新的Activity
				//startActivity(intent);
				Global.Screen_Off_Flag = true;
				showToast("解锁成功");
				finish();
			} else {
				mLockPatternView
						.setDisplayMode(LockPatternView.DisplayMode.Wrong);
				if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL) {
					mFailedPatternAttemptsSinceLastTimeout++;
					int retry = LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT
							- mFailedPatternAttemptsSinceLastTimeout;
					if (retry >= 0) {
						if (retry == 0)
							showToast(getResources().getString(R.string.lockpattern_input_wrong_five_times));
						mHeadTextView.setText("密码错误，还可以再输入" + retry + "次");
						mHeadTextView.setTextColor(Color.RED);
						mHeadTextView.startAnimation(mShakeAnim);
					}

				}else{
					showToast("输入长度不够，请重试");
				}

				if (mFailedPatternAttemptsSinceLastTimeout >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) {
					mHandler.postDelayed(attemptLockout, 2000);
				} else {
					mLockPatternView.postDelayed(mClearPatternRunnable, 2000);
				}
			}
		}

		private void patternInProgress() {
		}

		@Override
		public void onPatternCellAdded(List<Cell> pattern) {
			// TODO Auto-generated method stub
			
		}
	};
	Runnable attemptLockout = new Runnable() {

		@Override
		public void run() {
			mLockPatternView.clearPattern();
			mLockPatternView.setEnabled(false);
			mCountdownTimer = new CountDownTimer(
					LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS + 1, 1000) {
				@Override
				public void onTick(long millisUntilFinished) {
					int secondsRemaining = (int) (millisUntilFinished / 1000) - 1;
					if (secondsRemaining > 0) {
						mHeadTextView.setText(secondsRemaining + " 秒后重试");
					} else {
						mHeadTextView.setText("请绘制手势密码");
						mHeadTextView.setTextColor(Color.WHITE);
					}
				}
				@Override
				public void onFinish() {
					mLockPatternView.setEnabled(true);
					mFailedPatternAttemptsSinceLastTimeout = 0;
				}
			}.start();
		}
	};
	
	/**
	 * 调用JS函数
	 * @param webview
	 */
	 @SuppressLint("NewApi") 
	private void testEvaluateJavascript(WebView webview) {
	    	webview.evaluateJavascript("getSumValue()", new ValueCallback<String>() {
				@Override
				public void onReceiveValue(String value) {
					Log.i(TAG, "onReceiveValue value=   " + value);
				}
			});
	    }
	 
	 @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 
		 if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_BACK)) {
			 //back键等于Home键的效果
			 Intent i= new Intent(Intent.ACTION_MAIN); 
			    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			    i.addCategory(Intent.CATEGORY_HOME); 
			    startActivity(i); 
			 return true;
		 }
		return super.onKeyDown(keyCode, event);
	}
	 
	/* @Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
		 
		 Intent i= new Intent(Intent.ACTION_MAIN); 
		    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		    i.addCategory(Intent.CATEGORY_HOME); 
		    startActivity(i); 
	}*/
}
