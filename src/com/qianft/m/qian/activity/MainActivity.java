package com.qianft.m.qian.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.qianft.m.qian.BaseApplication;
import com.qianft.m.qian.R;
import com.qianft.m.qian.common.Constant;
import com.qianft.m.qian.common.Global;
import com.qianft.m.qian.service.AppUpgradeService;
import com.qianft.m.qian.utils.HttpUtils;
import com.qianft.m.qian.utils.LogUtil;
import com.qianft.m.qian.utils.MySharePreData;
import com.qianft.m.qian.utils.SharePopMenu;
import com.qianft.m.qian.utils.SharePopMenu.shareBottomClickListener;
import com.qianft.m.qian.utils.StorageUtils;
import com.qianft.m.qian.utils.Util;
import com.qianft.m.qian.view.GlobalProgressDialog;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;

/**
 * 
 * @author 
 */
public class MainActivity extends BaseActivity implements OnClickListener,
		shareBottomClickListener {

	private String TAG = "Wing";
	private WebView mWebView;
	private ImageButton mRefreshBtn;
	private LinearLayout mNoNetworkLinearLayout;
	private String mAddress = Constant.Address;
	//private String mAddress = "file:///android_asset/html/index.html";
	//private String mAddress = "http://192.168.0.88:8011/Home/Index";
	private boolean DEBUG = true;
	private String mShareUrl;
	private String mTitle;
	private Bitmap mIcom;
	private long exitTime = 0;
	private IWXAPI wxApi;
	private GlobalProgressDialog mGlobalProgressDialog;
	private SharePopMenu popMenu;
	private Context mContext;
	private boolean flag = true;
	public static final int TAKE_PHOTO = 0x00001007;
	public static final int CROP_PHOTO = 0x00001008;
	private Uri imageUri;
	private ImageView picture;
	private String mImageUrl = null;
	private String mDescription = null;
	private RequestQueue requestQueue = null;
	private String downloadUrl = null;
	private String newVersionName = null;
	private String packageSize = null;
	private String[] updateContentDetail = new String[3];  /*{"提升速度和稳定性", "增加消息推送等新功能", "修复部分Bug"}*/
	private int newVersionCode = 1;
	private PushAgent mPushAgent;
	private boolean pushFlag= true;
	private String mAuthCallback = null;
	private String mAuthCancel = null ;
	private ValueCallback<Uri> mUploadMessage;
	public static boolean isActive = true; //activity是否在后台
	public static boolean Screen_Off_Flag = true;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constant.HTML_LOADING:
				startProgressDialog();
				break;
			case Constant.HTML_LOADED:
				stopProgressDialog();
				break;
			case Constant.NO_NETWORK_HANDLER:
				LogUtil.d("Wing", "mNoNetworkLinearLayout mHandler errorCode");
				mNoNetworkLinearLayout.setVisibility(View.VISIBLE);
				break;

			case Constant.UPDATE_DIALOG_HANDLER:
				JSONObject jsonObject = (JSONObject) msg.obj;
				try {
					downloadUrl = jsonObject.getString("DownloadUrl");
					newVersionName = jsonObject.getString("New_VersionName");
					newVersionCode = jsonObject.getInt("New_VersionCode");
					packageSize = jsonObject.getString("Package_Size");
					String updateContent = jsonObject.getString("Update_Content");
					//String updateContent = jsonObject.getString(Update_Content);
					if (jsonObject.has("Update_Content") && updateContent.contains("\r\n")) {
						updateContentDetail = updateContent.split("\r\n", 3);
					}
					LogUtil.d("Wing", "response:  " + jsonObject.toString()
							+ "-----" + downloadUrl + "-----" + newVersionName
							+ "-----" + newVersionCode + "-----" + packageSize
							+ "-----" + updateContentDetail.toString());
					upgradeVersion();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			default:
				break;
			}
		};
	};

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		if(Build.VERSION.SDK_INT>=23){
			String[] mPermissionList = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
					Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CALL_PHONE,
					Manifest.permission.READ_LOGS,Manifest.permission.READ_PHONE_STATE, 
					Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.SET_DEBUG_APP,
					Manifest.permission.SYSTEM_ALERT_WINDOW,Manifest.permission.GET_ACCOUNTS,
					Manifest.permission.WRITE_APN_SETTINGS};
            ActivityCompat.requestPermissions(this,mPermissionList,100);
        }
		registerScreenBroadcast();
		mPushAgent = PushAgent.getInstance(this);
		initView();
		initData();
	}

	private void initView() {
		picture = (ImageView) findViewById(R.id.image_view);
		// mDrawer = (DrawerLayout) findViewById(R.id.navigation_drawer_layout);
		mWebView = (WebView) findViewById(R.id.webview);
		mNoNetworkLinearLayout = (LinearLayout) findViewById(R.id.no_network);
		mRefreshBtn = (ImageButton) findViewById(R.id.refresh_btn);
		mRefreshBtn.setOnClickListener(this);
		if (!Util.isNetWorkConnected(this)) {
			mNoNetworkLinearLayout.setVisibility(View.VISIBLE);
		} else {
			setWebView();
		}
		wxApi = WXAPIFactory.createWXAPI(this, Constant.APP_ID);
		wxApi.registerApp(Constant.APP_ID);
		mContext = this;
		// 初始化弹出菜单
		//popMenu = new SharePopMenu(this);
		/*
		 * popMenu.addItem(new ShareObject(getResources().getString(
		 * R.string.share_to_wechat_freind), R.drawable.share_to_freind));
		 * popMenu.addItem(new ShareObject(getResources().getString(
		 * R.string.share_to_wechat_circle), R.drawable.share_to_circle));
		 */
		//popMenu.setShareBottomClickListener(this);
	}
	
	/*public void registerScreenBroadcast() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mScreenBroadcast, filter);
	}*/
	
	public void umengShare(View view) {
		
		//testEvaluateJavascript(mWebView);
		startActivity(new Intent(MainActivity.this, CreateGesturePasswordActivity.class));
		/*final SHARE_MEDIA[] displaylist = new SHARE_MEDIA[]
                {
                    SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE,SHARE_MEDIA.SINA,
                    SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE
                };
        new ShareAction(this).setDisplayList( displaylist )
                .withText( "呵呵" )
                .withTitle("title")
                .withTargetUrl("http://www.baidu.com")
                //.withMedia()
                .setListenerList(umShareListener)
                .open();*/
		
		//Intent i = new Intent(MainActivity.this, TestActivity.class);
		//startActivity(i);
		
	}
	
	 private UMShareListener umShareListener = new UMShareListener() {
	        @Override
	        public void onResult(SHARE_MEDIA platform) {
	            Log.d("plat","platform"+platform);
	            Toast.makeText(MainActivity.this,  " 分享成功", Toast.LENGTH_SHORT).show();
	        }

	        @Override
	        public void onError(SHARE_MEDIA platform, Throwable t) {
	            Toast.makeText(MainActivity.this," 分享失败", Toast.LENGTH_SHORT).show();
	            if(t!=null){
	                Log.d("throw","throw:"+t.getMessage());
	            }
	        }
	        @Override
	        public void onCancel(SHARE_MEDIA platform) {
	            Toast.makeText(MainActivity.this," 分享取消", Toast.LENGTH_SHORT).show();
	        }
	    };
	    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get( this ).onActivityResult(requestCode, resultCode, data);
        Log.d("result","onActivityResult");
        
        if (requestCode == 0) {
			if (null == mUploadMessage){
				return;
			}
			Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
			mUploadMessage.onReceiveValue(result);
			mUploadMessage = null;
		}
    }

	private void initData() {
		EventBus.getDefault().register(this);
		requestQueue = Volley.newRequestQueue(mContext);
		PushAgent mPushAgent = PushAgent.getInstance(this);
		mPushAgent.enable(mRegisterCallback);
		//mPushAgent.enable();
		Log.i(TAG, "updateStatus:" + String.format("enabled:%s  isRegistered:%s  device_token:%s",
				mPushAgent.isEnabled(), mPushAgent.isRegistered(), mPushAgent.getRegistrationId()));
		cheakVersion();
	}

	/**
	 * 当Activity执行onResume()时让WebView执行resume
	 */
	@Override
	protected void onResume() {
		super.onResume();

		Bundle bun = getIntent().getExtras();
		String action = getIntent().getAction();
		Log.i(TAG, "action::  -------  "   + action);
		if (pushFlag && action != null && action.equals("com.qianft.m.qian.push")) {
			String Push_Url = getIntent().getStringExtra("Push_Url");
			mWebView.loadUrl(Push_Url);
			pushFlag = false;
		}
		if (action != null && action.equals("com.qianft.m.qian.login")) {
			String login_url = getIntent().getStringExtra("login_url");
			mWebView.loadUrl(login_url); 
		}
		
		if(!isActive && BaseApplication.getInstance().
				getLockPatternUtils().savedPatternExists()){  
            //从后台唤醒  
            isActive = true; 
            //Screen_Off_Flag = true;
            LogUtil.d(TAG, "onResume:  start UnlockGesturePasswordActivity");
            Intent intent = new Intent(this, UnlockGesturePasswordActivity.class);  
            startActivity(intent);    
        }   
        //友盟统计
		MobclickAgent.onResume(this);
		LogUtil.d(TAG, "onResume:  is executed");
	}
	
	public IUmengRegisterCallback mRegisterCallback = new IUmengRegisterCallback() {
			
			@Override
			public void onRegistered(String registrationId) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						Log.d("device_token", "device_token:   ---------->>>>>>>>>>>>>>>>>" );
					}
				});
			}
		};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.refresh_btn:
				if (!Util.isNetWorkConnected(this)) {
					mNoNetworkLinearLayout.setVisibility(View.VISIBLE);
				} else {
					mNoNetworkLinearLayout.setVisibility(View.INVISIBLE);
					setWebView();
				}
				break;
			default:
				break;
		}
	}

	/**
	 * 分享到微信朋友圈
	 */
	@Override
	public void shareCircle() {
		Log.d("Wing", "SHARE_TO_FREIND_CIRCLE");
		/*if (mShareUrl != null && mTitle != null && mDescription != null
				&& mImageUrl != null) {
			share(Constant.SHARE_TO_FREIND_CIRCLE, mShareUrl, mTitle,
					mDescription, mImageUrl);
			LogUtil.d("Wing", "mShareUrl: " + mShareUrl + "mTitle: " + mTitle
					+ "mDescription: " + mDescription + "mImageUrl: "
					+ mImageUrl);
		}*/
		new ShareAction(this)
		.setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)
		.setCallback(umShareListener)
		.withText("hello umeng video")
		.withTargetUrl("http://www.baidu.com")
		//.withMedia(image)
		.share();
	}
	/**
	 * 分享给微信好友
	 */
	@Override
	public void shareFreind() {
		Log.d("Wing", "SHARE_TO_FREIND");
		if (mShareUrl != null && mTitle != null && mDescription != null
				&& mImageUrl != null) {
			share(Constant.SHARE_TO_FREIND, mShareUrl, mTitle, mDescription,
					mImageUrl);
			LogUtil.d("Wing", "mShareUrl: " + mShareUrl + "mTitle: " + mTitle
					+ "mDescription: " + mDescription + "mImageUrl: "
					+ mImageUrl);
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void setWebView() {
		try {
			WebSettings webSettings = mWebView.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setDefaultTextEncodingName("utf-8");
			webSettings.setLoadWithOverviewMode(true);
			//webSettings.setAllowFileAccess(false);
			webSettings.setUseWideViewPort(false);
			//webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
			webSettings.setBlockNetworkImage(true);
			webSettings.setSavePassword(false);
			/*
			 * if (Build.VERSION.SDK_INT <= 18) {
			 * webSettings.setSavePassword(false); } else { // do nothing.
			 * because as google mentioned in the documentation - //
			 * "Saving passwords in WebView will not be supported in future versions"
			 * }
			 */
			Log.i(TAG, "MainActivity ----->>>>>setWebview");
			mWebView.addJavascriptInterface(/*getHtmlObject()*/new getHtmlObject(), "jsObj");
			mWebView.loadUrl(mAddress);
			mWebView.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK 
							&& mWebView.getUrl().equals("http://m.qianft.com/")) {
						return false;
					} else if (keyCode == KeyEvent.KEYCODE_BACK
							&& mWebView.canGoBack()) {
						mWebView.goBack();
						mWebView.loadUrl("javascript:window.history.back();");
						return true;
					}
					return false;
				}
			});
			mWebView.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if (DEBUG)
						Log.e("Wing", "MainActivity..shouldOverrideUrlLoading..  url=" + url);
					view.loadUrl(url);
					return true;
				}

				@Override
				public void onPageStarted(WebView view, String url,
						Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					mHandler.sendEmptyMessage(Constant.HTML_LOADING);
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					CookieManager cookieManager = CookieManager.getInstance();
					String CookieStr = cookieManager.getCookie(url);
					Log.e("Wing", "Cookies = " + CookieStr);
					mWebView.getSettings().setBlockNetworkImage(false);
					super.onPageFinished(view, url);
				}
				@Override
				@Deprecated
				public void onReceivedError(WebView view, int errorCode,
						String description, String failingUrl) {
					/*
					 * super.onReceivedError(view, errorCode, description,
					 * failingUrl);
					 */
					if (view != null) {
						view.stopLoading();
						view.clearView();
					}
					LogUtil.d("Wing", "onReceivedError---errorCode---->>>>>>>"
							+ errorCode);
					if (mHandler != null) {
						mHandler.sendEmptyMessage(Constant.NO_NETWORK_HANDLER);
					}
					if (mNoNetworkLinearLayout != null) {
						mNoNetworkLinearLayout.setVisibility(View.VISIBLE);
					}
					super.onReceivedError(view, errorCode, description,
							failingUrl);
				}
			});
			mWebView.setWebChromeClient(new WebChromeClient() {
				@Override
				public void onReceivedTitle(WebView view, String title) {
					super.onReceivedTitle(view, title);
					Log.d("Wing", "Title:  " + title);
					// mTitle = title;
				}

				@Override
				public void onReceivedIcon(WebView view, Bitmap icon) {
					super.onReceivedIcon(view, icon);
					// mIcom = icon;
				}

				@Override
				public void onProgressChanged(WebView view, int newProgress) {
					if (newProgress == 100) {
						mHandler.sendEmptyMessage(Constant.HTML_LOADED);
					}
					super.onProgressChanged(view, newProgress);
				}

				@Override
				public boolean onJsAlert(WebView view, String url,
						String message, JsResult result) {
					Log.d(TAG, "onJsAlert message: " + message);
					return super.onJsAlert(view, url, message, result);
				}

				@Override
				public boolean onJsConfirm(WebView view, String url,
						String message, JsResult result) {
					return super.onJsConfirm(view, url, message, result);
				}
				
				@SuppressWarnings("unused")
				public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
					this.openFileChooser(uploadMsg);
				}

				@SuppressWarnings("unused")
				public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
					this.openFileChooser(uploadMsg);
				}

				public void openFileChooser(ValueCallback<Uri> uploadMsg) {
					mUploadMessage = uploadMsg;
					pickFile();
				}
			});

		} catch (Exception e) {
			return;
		}
	}
	
	public void pickFile() {
		Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
		chooserIntent.setType("image/*");
		startActivityForResult(chooserIntent, 0);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_BACK)) {
			if (mWebView != null) {
				LogUtil.d(TAG, "mWebView.getUrl();  "  + mWebView.getUrl());
				if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.getUrl().equals("http://m.qianft.com/")) {
					exitApp();
					return false;
				} else {
					if (mWebView.canGoBack()) {
						mWebView.loadUrl("javascript:window.history.back();");
					} else {
						mWebView.loadUrl("http://m.qianft.com/");
					}
					//return false;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 连续两次点击Back键退出App
	 */
	public void exitApp() {
		if ((System.currentTimeMillis() - exitTime) > 2000) {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.quit_app),
					Toast.LENGTH_SHORT).show();
			exitTime = System.currentTimeMillis();
		} else {
			finish();
			System.exit(0);
		}
	}

	/**
	 * 微信分享功能
	 * 
	 * @param flag
	 * @param url
	 * @param title
	 * @param description
	 * @param imageUrl
	 */
	private void share(int flag, String url, String title, String description,
			String imageUrl) {
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = url;
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = title;
		msg.description = description;

		Bitmap thumb = BitmapFactory.decodeResource(getResources(),
				R.drawable.app_icon);
		// msg.setThumbImage(thumb);
		Bitmap thumb2 = null;
		try {
			thumb2 = Glide.with(mContext).load(imageUrl).asBitmap() // 必须
					.centerCrop().into(500, 500).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (thumb2 != null) {
			LogUtil.d("Wing", "thumb2 != null  -------->>>>>>>>>>>");
			msg.setThumbImage(thumb2);
		} else {
			// msg.thumbData = Util.bmpToByteArray(thumb, true);
			LogUtil.d("Wing", "thumb2 = null  -------->>>>>>>>>>>++++++++");
			msg.setThumbImage(thumb);
		}
		// msg.setThumbImage(thumb);
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("webpage");
		req.message = msg;
		req.scene = flag == 0 ? SendMessageToWX.Req.WXSceneSession
				: SendMessageToWX.Req.WXSceneTimeline;
		boolean fla = wxApi.sendReq(req);
		System.out.println("fla=" + fla);
	}

	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis())
				: type + System.currentTimeMillis();
	}

	/**
	 * 
	 * 
	 */
	private void loginWechat() {
		// send oauth request
		final SendAuth.Req req = new SendAuth.Req();
		req.scope = "snsapi_userinfo";
		req.state = "wechat_sdk_demo_test";
		wxApi.sendReq(req);
	}

	private void loginWechat_2() {
		// send oauth request
		final SendAuth.Req req = new SendAuth.Req();
		req.scope = "snsapi_userinfo";
		req.state = "login_state";
		wxApi.sendReq(req);
	}

	/**
	 * 与js交互的对象
	 * 
	 * @return
	 */
	 public class getHtmlObject {
		 protected Context ctx;
		 protected WebView vw;
		 
		 public getHtmlObject() {
			 
		 }
		 public getHtmlObject(Context context, WebView webview) {
			 this.ctx = context;
			 this.vw = webview;
			 
		 }

		//Object insertObj = new Object() {
			@JavascriptInterface
			public void Js_Invoke_Android_Main_Interface(String functionName, String json) {
				LogUtil.d("Wing", "HtmlcallAndroid---------->>>>>>>>>>>>>>" +functionName + "-------" +  json);
				JSONObject jsonObject = null;
				JSONObject returnJson = null;
				String mCallback = null ;
				try {
					returnJson = new JSONObject();
					jsonObject = new JSONObject(json);
					mCallback = jsonObject.getString("callback");
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				switch (functionName) {
						case "share_To_Wechat_android":
							share_To_Wechat_android(json);
							break;
						case "loginWechat_android":
							loginWechat_android(json);
							break;
						case "wechat_Auth_Login_android":
							wechat_Auth_Login_android(json);
							break;
						case "takePhoto_android":
							takePhoto_android(json);
							break;
						case "HtmlcallJava2":
							LogUtil.d("Wing", "HtmlcallJava2---------->>>>>>>>>>>>>>>>" + json);
							HtmlcallJava2(json);
							break;
						case "getUserAPPInfo_android":
							getUserAPPInfo_android(json);
							break;
						case "startQQDialog_android":
							startQQDialog_android(json);
						   break;
						case "clearUserInfo_android":
							clearUserInfo_android(json);
						   break;
						case "startGesturePasswordSetup_android":
							startGesturePasswordSetup_android(json);
						   break;
						   
						default:
							try {
								returnJson.put("errorCode", "0002");
								returnJson.put("errorMsg", "This version nonsupport function");
							} catch (JSONException e) {
								e.printStackTrace();
							}finally {
								if (!TextUtils.isEmpty(mCallback) && returnJson != null) {
									String result = returnJson.toString();
									mWebView.loadUrl("javascript:" + mCallback + "(" + result +")" );
								}
							}
						 break;
				}
			}
			
			@JavascriptInterface
			public void share_To_Wechat_android(final String webpageUrl,
					final String title, final String description, final String imageUrl) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						mTitle = title;
						mShareUrl = webpageUrl;
						mDescription = description;
						mImageUrl = imageUrl;
						/*popMenu.showAsDropDown(MainActivity.this
								.findViewById(R.id.main_root));*/
						
						final SHARE_MEDIA[] displaylist = new SHARE_MEDIA[]
				                {
				                    SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE,SHARE_MEDIA.SINA,
				                    SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE
				                };
				        new ShareAction(MainActivity.this).setDisplayList( displaylist )
				                .withText(description)
				                .withTitle(title)
				                .withTargetUrl(webpageUrl)
				                .withMedia(new UMImage(MainActivity.this,
				                        BitmapFactory.decodeResource(getResources(), R.drawable.app_icon)))
				                .setListenerList(umShareListener)
				                .open();
					}
				});
				
			}
			
			public void  share_To_Wechat_android(final String json) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						JSONObject returnJson = null;
						String mCallback = null;
						String mCancel = null ;
						String result = null;
						try {
							JSONObject jsonObject = new JSONObject(json);
							mShareUrl = jsonObject.getString("link");
							mTitle = jsonObject.getString("title");
							mDescription = jsonObject.getString("desc");
							mImageUrl = jsonObject.getString("imgUrl");
							
							
							if (jsonObject.has("callback")) {
								mCallback = jsonObject.getString("callback");
							}
							if (jsonObject.has("cancel")) {
								mCancel = jsonObject.getString("cancel");
							}
							
							final SHARE_MEDIA[] displaylist = new SHARE_MEDIA[]
					                {
					                    SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE,SHARE_MEDIA.SINA,
					                    SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE
					                };
					        new ShareAction(MainActivity.this).setDisplayList( displaylist )
					                .withText(mDescription)
					                .withTitle(mTitle)
					                .withTargetUrl(mShareUrl)
					                .withMedia(new UMImage(MainActivity.this,
					                        BitmapFactory.decodeResource(getResources(), R.drawable.app_icon)))
					                .setListenerList(umShareListener)
					                .open();
							/*popMenu.showAsDropDown(MainActivity.this
									.findViewById(R.id.main_root));*/
							
							returnJson = new JSONObject();
							returnJson.put("errCode", "0000");
							returnJson.put("errMsg", "执行成功");
							
							
							JSONObject jsonObject2 = new JSONObject();
							jsonObject2.put("link", mShareUrl);
							jsonObject2.put("title", mTitle);
							jsonObject2.put("desc", mDescription);
							
							returnJson.put("ref", jsonObject2);
							
						} catch (Exception e) {
							try {
								returnJson.put("errCode", "0001");
								returnJson.put("errMsg", "NO");
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							e.printStackTrace();
							final String errorMsg = getErrorInfo(e);
							
							
					        Map<String, String> map = new HashMap<String, String>(); 
					        map.put("logInfo ", errorMsg);  
	        	            map.put("type", "2");  
					        try {
								String response = HttpUtils.postRequest(Constant.ERROR_MSG_POST_URL,
										map, MainActivity.this);
								LogUtil.d("Wing", "--post commit---" + response);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}finally {
							if (!TextUtils.isEmpty(mCallback) && returnJson != null) {
								result = returnJson.toString();
								mWebView.loadUrl("javascript:" + mCallback + "(" + result +")" );
							}
						}
					}
				});
			}

			public void loginWechat_android(final String json) {

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						JSONObject returnJson = null;
						String mCallback = null;
						String mCancel = null ;
						String result = null;
						try {
							JSONObject jsonObject = new JSONObject(json);
							String mLoginUrl = jsonObject.getString("loginUrl");
							if (jsonObject.has("callback")) {
								mCallback = jsonObject.getString("callback");
							}
							if (jsonObject.has("cancel")) {
								mCancel = jsonObject.getString("cancel");
							}
							String uid = MySharePreData.GetData(MainActivity.this,
									Constant.WECHAT_LOGIN_SP_NAME, "union_id");
							if (TextUtils.isEmpty(uid)) {
								loginWechat_2();
							} else {
								Log.d(TAG, "union_id:   " + uid);
								mWebView.loadUrl(mLoginUrl.replace("UNIONID", uid));
							}
							returnJson = new JSONObject();
						    returnJson.put("userVersionCode", Global.localVersionCode);
							returnJson.put("userVersionName", Global.localVersionName);
							returnJson.put("errCode", "0000");
							returnJson.put("errMsg", "执行成功");
							
							JSONObject jsonObject2 = new JSONObject();
							jsonObject2.put("loginUrl", mLoginUrl);
							returnJson.put("ref", jsonObject2);
						} catch (Exception e) {
							try {
								returnJson.put("errCode", "0004");
								returnJson.put("errMsg", "登录失败");
							} catch (JSONException e1) {
								e1.printStackTrace();
							}
							e.printStackTrace();
							
							final String errorMsg = getErrorInfo(e);
							
					        Map<String, String> map = new HashMap<String, String>(); 
					        map.put("logInfo ", errorMsg);  
	        	            map.put("type", "2");  
					        try {
								String response = HttpUtils.postRequest(Constant.ERROR_MSG_POST_URL,	
										map, MainActivity.this);
								LogUtil.d("Wing", "--post commit---" + response);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}finally{
							if (!TextUtils.isEmpty(mCallback) && returnJson != null) {
								result = returnJson.toString();
								mWebView.loadUrl("javascript:" + mCallback + "(" + result +")" );
							}
						}
					}
				});
			}
			@JavascriptInterface
			public void takePhoto_android(String path, String picFileName) {
				takePhoto(path, picFileName);
			}
			public void takePhoto_android(final String json) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						JSONObject returnJson = null;
						JSONObject jsonObject;
						String mCallback = null;
						String result;
						try {
							jsonObject = new JSONObject(json);
							String mSaveTargetDir = jsonObject.getString("saveTargetDir");
							String mPicFileName = jsonObject.getString("picFileName");
							
							if (jsonObject.has("callback")) {
								mCallback = jsonObject.getString("callback");
							}
							takePhoto(mSaveTargetDir, mPicFileName);
							
							returnJson = new JSONObject();
							returnJson.put("errCode", "0000");
							returnJson.put("errMsg", "执行成功");

							JSONObject jsonObject2 = new JSONObject();
							jsonObject2.put("saveTargetDir", mSaveTargetDir);
							jsonObject2.put("picFileName", mPicFileName);
							
							returnJson.put("ref", jsonObject2);
							
						} catch (Exception e) {
							e.printStackTrace();
							try {
								returnJson.put("errCode", "0006");
								returnJson.put("errMsg", "保存失败");
							} catch (JSONException e2) {
								// TODO Auto-generated catch block
								e2.printStackTrace();
							}
							final String errorMsg = getErrorInfo(e);
					        Map<String, String> map = new HashMap<String, String>(); 
					        map.put("logInfo ", errorMsg);  
	        	            map.put("type", "2");  
					        try {
								String response = HttpUtils.postRequest(Constant.ERROR_MSG_POST_URL,
										map, MainActivity.this);
								LogUtil.d("Wing", "--post commit---" + response);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						} finally {
							
							if (!TextUtils.isEmpty(mCallback) && returnJson != null) {
								result = returnJson.toString();
								mWebView.loadUrl("javascript:" + mCallback + "(" + result +")" );
							}
						}
						
						
					}
				});
				
			}

			// 微信授权
			/*@JavascriptInterface
			public String wechat_Auth_Login_android(String userid) {
				Log.d("Wing", "userid:   " + userid);
				Util.USER_ID = userid;
				loginWechat();
				return "";
			}*/
			@JavascriptInterface
			public void  wechat_Auth_Login_android(final String json) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						LogUtil.d(TAG, "wechat_Auth_Login_android  ------------------json"  + json);
						String mCallback = null;
						String mCancel = null;
						JSONObject jsonObject = null;
						JSONObject returnJson = null;
						
						try {
							jsonObject = new JSONObject(json);
							//String userid = jsonObject.getString("userId");
							//String postServerUrl = jsonObject.getString("postServerUrl");
							//String auth_Success_Url = jsonObject.getString("auth_Success_Url");
							LogUtil.d(TAG, "jsonObject.has(callback) "  + jsonObject.has("callback"));
							if (jsonObject.has("callback")) {
								mAuthCallback = jsonObject.getString("callback");
							}
							LogUtil.d(TAG, "jsonObject.has(cancel): "   + jsonObject.has("cancel"));
							if (jsonObject.has("cancel")) {
								mAuthCancel = jsonObject.getString("cancel");
							}
							//LogUtil.d("Wing", "userid:   " + userid);
							//Util.SERVER_URL = postServerUrl;
							//Util.Auth_Success_Url = auth_Success_Url;
							//Util.USER_ID = userid;
							loginWechat();
							
							returnJson = new JSONObject();
							returnJson.put("errCode", "0000");
							returnJson.put("errMsg", "执行成功");

							//JSONObject jsonObject2 = new JSONObject();
							//jsonObject2.put("userId", userid);
							//jsonObject2.put("postServerUrl", postServerUrl);
							//jsonObject2.put("auth_Success_Url", auth_Success_Url);
							
							//returnJson.put("ref", jsonObject2);
							
							//if (!TextUtils.isEmpty(mCallback)) {
								//mWebView.loadUrl("javascript:" + mSuccess + "(" + result +")" );
							//}
						} catch (JSONException e) {
							try {
								returnJson.put("errCode", "0003");
								returnJson.put("errMsg", "授权失败");
							} catch (JSONException e2) {
								e2.printStackTrace();
							}
							e.printStackTrace();
							final String errorMsg = getErrorInfo(e);
					        Map<String, String> map = new HashMap<String, String>(); 
					        map.put("logInfo ", errorMsg);  
	        	            map.put("type", "2");  
					        try {
								String response = HttpUtils.postRequest(Constant.ERROR_MSG_POST_URL,
										map, MainActivity.this);
								LogUtil.d("Wing", "--post commit---" + response);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}finally {
							if (!TextUtils.isEmpty(mCallback) && returnJson != null) {
								LogUtil.d(TAG, "returnJson:   " + returnJson);
								String result = returnJson.toString();
								mWebView.loadUrl("javascript:" + mCallback + "(" + result +")" );
							}
						}
					}
				});
			}

			public void  HtmlcallJava2(final String json) {
				
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						JSONObject returnJson = null;
						String mCallback = null;
						Log.d("Wing", "success:  "  + "::::" + json);
						try {
							returnJson = new JSONObject();
							
							//Log.d("Wing", "HtmlcallJava2: " + java + "::::" + C + "::::" + PHP);
							JSONObject jsonObject = new JSONObject(json);
							Log.d("Wing", "jsonObject:  " + jsonObject);
							String java = jsonObject.getString("java");
							String C = jsonObject.getString("C++");
							String PHP = jsonObject.getString("PHP");
							mCallback = jsonObject.getString("callback");
							
							Log.d("Wing", "HtmlcallJava2:   " + java + ":::: " + C + "::::" + PHP);
							//mWebView.loadUrl("javascript: " + success +"()");
							
							returnJson.put("success", "0000");
							returnJson.put("failure", "0001");
							
							JSONObject jsonObject2 = new JSONObject();
							jsonObject2.put("wwww", "wuyong");
							jsonObject2.put("yyyy", "wuwuuuu");
							
							returnJson.put("res", jsonObject2);  
							
						//	String reslut = returnJson.toString();
						//	Log.d("Wing", "reslut:  -----" + reslut);
						//	mWebView.loadUrl("javascript: " + mCallback + "(" + reslut + ")");
							/*mWebView.evaluateJavascript("fromAndroid()", new ValueCallback<String>() {
							    @Override
							    public void onReceiveValue(String value) {
							        //store / process result received from executing Javascript.
							    }
							});*/
							
						} catch (Exception e) {
							e.printStackTrace();
							final String errorMsg = getErrorInfo(e);
					        Map<String, String> map = new HashMap<String, String>(); 
					        map.put("logInfo ", errorMsg);  
	        	            map.put("type", "2");  
					        try {
								String response = HttpUtils.postRequest(Constant.ERROR_MSG_POST_URL,
										map, MainActivity.this);
								LogUtil.d("Wing", "--post commit---" + response);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						} finally {
							LogUtil.d(TAG, "finally is running" + "returnJson:  " + returnJson + "mCallback:  " +mCallback);
							if (!TextUtils.isEmpty(mCallback) && returnJson != null) {
								
								LogUtil.d(TAG, "finally is running");
								String result = returnJson.toString();
								mWebView.loadUrl("javascript:" + mCallback + "(" + result +")" );
							}
						}
					}
				});
			}

			@JavascriptInterface
			public void JavacallHtml(JSONObject js) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mWebView.loadUrl("javascript: showFromHtml()");
						Toast.makeText(MainActivity.this, "clickBtn",
								Toast.LENGTH_SHORT).show();
					}
				});
			}

			@JavascriptInterface
			public void JavacallHtml2() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mWebView.loadUrl("javascript: showFromHtml2('qian fu tong')");
						Toast.makeText(MainActivity.this, "clickBtn2",
								Toast.LENGTH_SHORT).show();
					}
				});
			}

			/**
			 * This is not called on the UI thread. Post a runnable to invoke
			 * loadUrl on the UI thread.
			 */
			@JavascriptInterface
			public String clickOnAndroid() {
				mHandler.post(new Runnable() {
					public void run() {
					}
				});
				return "aaaaa";
			}

			/**
			 * 图片下载
			 * 
			 * @param imageUrl
			 * @param savePath
			 * @param picFileName
			 * @return
			 */
			/*@JavascriptInterface
			public void downloadPicture(final String imageUrl,
					final String savePath, final String picFileName) {
				Util.downLoadPicture(imageUrl, savePath, picFileName);
			}*/
			
			@JavascriptInterface
			public void downloadPicture_android(final String json) {
				
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						JSONObject returnJson = new JSONObject();
						JSONObject jsonObject = null;
						String mCallback = null;
						try {
							jsonObject = new JSONObject(json);
							String imageUrl = jsonObject.getString("imageUrl");
							String savePath = jsonObject.getString("savePath");
							String picFileName = jsonObject.getString("picFileName");
							long totalSize = jsonObject.getInt("totalSize");
							
							if (jsonObject.has("callback")) {
								mCallback = jsonObject.getString("callback");
							}
							File mTempFile = new File(picFileName);
							returnJson = new JSONObject();
							
							returnJson.put("errCode", "0000");
							returnJson.put("errMsg", "执行成功");
							
							JSONObject jsonObject2 = new JSONObject();
							jsonObject2.put("imageUrl", imageUrl);
							jsonObject2.put("savePath", savePath);
							jsonObject2.put("picFileName", picFileName);
							jsonObject2.put("totalSize", totalSize);
							
							returnJson.put("ref", jsonObject2);
							
							 long storage = StorageUtils.getAvailableStorage();
						        if (DEBUG) {
						            Log.i(TAG, "storage:" + storage + " totalSize:" + totalSize);
						        }

						        if (totalSize - mTempFile.length() > storage) {
						        	returnJson.put("errCode", "0007");
						        	returnJson.put("errMsg", "存储不足");
						            //throw new NoMemoryException("SD card no memory.");
						        }
							
							Util.downLoadPicture(imageUrl, savePath, picFileName);
							
						}catch (Exception e) {
							try {
								returnJson.put("errCode", "0009");
					        	returnJson.put("errMsg", "保存失败");
							} catch (JSONException e1) {
								e1.printStackTrace();
							}
							final String errorMsg = getErrorInfo(e);
					        Map<String, String> map = new HashMap<String, String>(); 
					        map.put("logInfo ", errorMsg);  
	        	            map.put("type", "2");  
					        try {
								String response = HttpUtils.postRequest(Constant.ERROR_MSG_POST_URL,
										map, MainActivity.this);
								LogUtil.d("Wing", "--post commit---" + response);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						} finally {
								if (!TextUtils.isEmpty(mCallback) && returnJson != null) {
								
								LogUtil.d(TAG, "finally is running");
								String result = returnJson.toString();
								mWebView.loadUrl("javascript:" + mCallback + "(" + result +")" );
							}
						}
					}
				});
				
			}
			@JavascriptInterface
			public void clearUserInfo_android() {
				
				MySharePreData.SetData(mContext, Constant.WECHAT_LOGIN_SP_NAME,
						"union_id", "");
			}
			
			public void clearUserInfo_android(String json) {
				
				JSONObject jsonObject = null;
				JSONObject returnJson = null;
				String mCallback = null;
				try {
					jsonObject = new JSONObject(json);
					if (jsonObject.has("callback")) {
						mCallback = jsonObject.getString("callback");
					}
					returnJson = new JSONObject();
					returnJson.put("errCode", "0000");
					returnJson.put("errMsg", "执行成功");
					MySharePreData.SetData(mContext, Constant.WECHAT_LOGIN_SP_NAME,
							"union_id", "");
					String result = returnJson.toString();
					mWebView.loadUrl("javascript:" + mCallback + "(" + result +")" );
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				
			}

			@JavascriptInterface
			public void startQQDialog_android(String qqId) {
				String url = "mqqwpa://im/chat?chat_type=wpa&uin=UIN".replace(
						"UIN", qqId);
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
			}
			
			/*public void startQQDialog_android(String json) {
				
				JSONObject jsonObject = null;
				JSONObject returnJson = null;
				String mCallback = null;
				try {
					jsonObject = new JSONObject(json);
					
					String mQQID = jsonObject.getString("qqId");
					if (jsonObject.has("callback")) {
						mCallback = jsonObject.getString("callback");
					}
					returnJson = new JSONObject();
					returnJson.put("errCode", "0000");
					returnJson.put("errMsg", "执行成功");
					String url = "mqqwpa://im/chat?chat_type=wpa&uin=UIN".replace(
							"UIN", mQQID);
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
					String result = returnJson.toString();
					mWebView.loadUrl("javascript:" + mCallback + "(" + result +")" );
				} catch (JSONException e) {
					e.printStackTrace();
				}
			
			}*/
			
			@JavascriptInterface
			public void  getUserAPPInfo_android(final String json) {
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						JSONObject jsonObject = null;
						JSONObject returnJson = null;
						String mCallback = null;
						try {
							LogUtil.d("Wing", "json_info:  " + json);
							jsonObject = new JSONObject(json);
							if (jsonObject.has("callback")) {
								mCallback = jsonObject.getString("callback");
							}
							String phoneMode = android.os.Build.MODEL;
							String systemSDK = android.os.Build.VERSION.SDK;
							
							returnJson = new JSONObject();
							returnJson.put("errCode", "0000"); 
							returnJson.put("errMsg", "执行成功");
							
							JSONObject returnRefJson = new JSONObject();
							returnRefJson.put("userVersionCode", Global.localVersionCode);
							returnRefJson.put("userVersionName", "v" + Global.localVersionName);
							returnJson.put("ref", returnRefJson);
							String result = returnJson.toString();
							mWebView.loadUrl("javascript:" + mCallback + "(" + result +")" );
						} catch (JSONException e) {
							e.printStackTrace();
						}
						
					}
				});
			}
			public void startGesturePasswordSetup_android(final String json) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							startActivity(new Intent(MainActivity.this, CreateGesturePasswordActivity.class));
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
				});
			}
			
			@JavascriptInterface
			public void startGesturePasswordSetup_android() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							startActivity(new Intent(MainActivity.this, CreateGesturePasswordActivity.class));
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
				});
			}
			
			@JavascriptInterface
			public void isSettingGesturePSW_android(final String json) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						JSONObject jsonObject = null;
						JSONObject returnJson = null;
						String mCallback = null;
						try {
							jsonObject = new JSONObject(json);
							if (jsonObject.has("callback")) {
								mCallback = jsonObject.getString("callback");
							}
							returnJson = new JSONObject();
							if (BaseApplication.getInstance().getLockPatternUtils().savedPatternExists()) {
								returnJson.put("is_setting_gesture_password", true);
							} else {
								returnJson.put("is_setting_gesture_password", false);
							}
							Log.d("Wing", "is_setting_gesture_password:  " + returnJson.toString());
							mWebView.loadUrl("javascript: " + mCallback + "(" + returnJson.toString() + ")");
						} catch (Exception e) {
							e.printStackTrace();   
						}
					}
				});
			}
			
			@JavascriptInterface
			public void changeGesturePassword_android() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							BaseApplication.getInstance().getLockPatternUtils().clearLock();
							startActivity(new Intent(MainActivity.this, CreateGesturePasswordActivity.class));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
			
			private Map<String,String> valueMap = new HashMap<String, String>();  
			
			@JavascriptInterface
			public String set(String key, String value) {  
			        valueMap.put(key, value);  
			        return "";  
		  
		    }  
			
			@JavascriptInterface
		    public String get(String key){  
		        return valueMap.get(key);  
		    }
		//return insertObj;
	}

	// 开始网页加载进度条
	private void startProgressDialog() {
		if (mGlobalProgressDialog == null) {
			mGlobalProgressDialog = GlobalProgressDialog.createDialog(this);
		}
		mGlobalProgressDialog.show();  
	}

	// 停止网页加载进度条
	private void stopProgressDialog() {
		if (mGlobalProgressDialog != null) {
			if (mGlobalProgressDialog.isShowing()) {
				mGlobalProgressDialog.dismiss();
			}
			mGlobalProgressDialog = null;
		}
	}

	/**
	 * 
	 * @param path
	 * @param picFileName
	 */
	private void takePhoto(String saveTargetDir, String picFileName) {
		String rootPath = Environment.getExternalStorageDirectory().toString()
				+ "/";
		File pathDir = new File(rootPath + saveTargetDir);
		if (!pathDir.exists()) {
			pathDir.mkdirs();
		}
		File outputImage = new File(pathDir, picFileName + ".jpg");
		try {
			if (outputImage.exists()) {
				outputImage.delete();
			}
			outputImage.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		imageUri = Uri.fromFile(outputImage);
		
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		//startActivityForResult(intent, TAKE_PHOTO);
		startActivity(intent);
		
		//Bitmap saveBitmap =(Bitmap)params[0];
		//String picOuputPath =(String)params[1];
		FileOutputStream out = null;
		try {
			
			Bitmap saveBitmap = BitmapFactory
					.decodeStream(getContentResolver().openInputStream(
							imageUri));
			out = new FileOutputStream(saveTargetDir);
			saveBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			// Release bitmap
			if (saveBitmap != null && !saveBitmap.isRecycled()) {
				saveBitmap.recycle();
				saveBitmap = null;
			}
		} catch (Exception e) {
			LogUtil.e(TAG, "Failed to write image --- ");
		} finally {
			try {
				out.close();
			} catch (Exception e) {
			}
		}
		
	}

	/*@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case TAKE_PHOTO:
			if (resultCode == RESULT_OK) {
				Intent intent = new Intent("com.android.camera.action.CROP");
				intent.setDataAndType(imageUri, "image/*");
				intent.putExtra("scale", true);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				startActivityForResult(intent, CROP_PHOTO);
			}
			break;
		case CROP_PHOTO:
			if (resultCode == RESULT_OK) {
				try {
					Bitmap bitmap = BitmapFactory
							.decodeStream(getContentResolver().openInputStream(
									imageUri));
					picture.setImageBitmap(bitmap);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			break;
		default:
			break;
		}
	}*/

	public void synCookies(String url, String cookies) {
		Log.d("Wing", "synCookies:" + url);
		CookieSyncManager.createInstance(mContext);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		Log.d("Wing", "cookie:" + cookies);
		cookieManager.setCookie(url, cookies);// cookies是在HttpClient中获得的cookie?
		CookieSyncManager.getInstance().sync();

		Log.d("Wing", "cookie:" + cookieManager.getCookie(url));
	}

	// 清除所有cookie
	private void removeAllCookie() {
		CookieSyncManager cookieSyncManager = CookieSyncManager
				.createInstance(mContext);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		cookieManager.removeSessionCookie();

		// String testcookie1 = cookieManager.getCookie(urlpath);

		cookieManager.removeAllCookie();
		cookieSyncManager.sync();

		// String testcookie2 = cookieManager.getCookie(urlpath);
	}
	@Override
	protected void onPause() {
		super.onPause();
		//友盟统计
		Log.d("Wing", "onPause is executed! ");
		MobclickAgent.onPause(this);

	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.d("wing", "onStop execute!");  
		
		if(!isAppOnForeground()){  
            Log.d("wing", "onStop back");  
            isActive = false;  
        }  
	}
	
	  /** 
     * 是否在后台 
     * @return 
     */  
    public boolean isAppOnForeground(){  
        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);  
        String curPackageName = getApplicationContext().getPackageName();  
        List<RunningAppProcessInfo> app = am.getRunningAppProcesses();  
        if(app==null){  
            return false;  
        }  
        for(RunningAppProcessInfo a:app){  
            if(a.processName.equals(curPackageName)&&  
                    a.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
            	
            	Log.d("Wing", "isAppOnForeground------>>>>>>>>>>");
                return true;  
            }  
        }  
        return false;  
        /*ComponentName cn = am.getRunningTasks(1).get(0).topActivity; 
        if(!TextUtils.isEmpty(curPackageName)&&curPackageName.equals(getPackageName())){ 
            return true; 
        } 
        return false;*/  
    } 

	@Override
	protected void onDestroy() {
		super.onDestroy();
		removeAllCookie();
		EventBus.getDefault().unregister(this);
		
		/*if (mScreenBroadcast != null) {
			unregisterReceiver(mScreenBroadcast);
		}*/
	}
	
	/**
	 * EventBus2.0 微信授权
	 * @param message
	 */

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void helloEventBus(String message) {
		Log.d("Wing", "message:  " + message);
		switch (message) {
			case "hello":
				break;
			case "auth_cancel":
				JSONObject returnCancelJson = new JSONObject();
				try {
					returnCancelJson.put("errCode", "0000");
					returnCancelJson.put("errMsg", "登录取消");
					mWebView.loadUrl("javascript: " + mAuthCancel + "(" + returnCancelJson.toString() + ")");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case "auth_fail":
				JSONObject returnFailJson = new JSONObject();
				try {
					returnFailJson.put("errCode", "0004");
					returnFailJson.put("errMsg", "登录失败");
					mWebView.loadUrl("javascript: " + mAuthCallback + "(" + returnFailJson.toString() + ")");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
		}
		/*if (message.equals("hello")) {
			mWebView.loadUrl("http://m.qianft.com/WeiXin/Success");
		} else if (message.equals("login_state")) {
			String uid = MySharePreData.GetData(MainActivity.this,
					Constant.WECHAT_LOGIN_SP_NAME, "union_id");
			mWebView.loadUrl("http://m.qianft.com/UserLogin/WeChatLogin?unionId=UNIONID"
					.replace("UNIONID", uid));
		} else if (message.equals("")) {
			mWebView.loadUrl("");
		}*/
	}
	
	/**
	 * 微信授权
	 * @param bundle
	 */
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void weChat(Bundle bundle) {
		
		if ((Global.RESP.errCode == BaseResp.ErrCode.ERR_OK) && 
				(Global.RESP.getType() == ConstantsAPI.COMMAND_SENDAUTH)) {
			JSONObject returnJson = new JSONObject();
			try {
				if (Global.RESP.errCode == BaseResp.ErrCode.ERR_COMM);
				returnJson.put("errCode", "0000");
				returnJson.put("errMsg", "执行成功");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			LogUtil.d(TAG, "bundle::::  " + bundle.toString());
			JSONObject jsonRef = new JSONObject();
			
			Log.d("Wing", "");
			try {
				jsonRef.put("openid", bundle.getString("openid"));
				jsonRef.put("province", bundle.getString("province"));
				jsonRef.put("unionid", bundle.getString("unionid"));
				jsonRef.put("sex", bundle.getString("sex"));
				jsonRef.put("city", bundle.getString("city"));
				jsonRef.put("nickname", bundle.getString("nickname"));
				jsonRef.put("country", bundle.getString("country"));
				jsonRef.put("headimgurl", bundle.getString("headimgurl"));
				returnJson.put("ref", jsonRef);
				Log.d("Wing", "returnJson.toString():   "  + returnJson.toString());
				mWebView.loadUrl("javascript: " + mAuthCallback + "(" + returnJson.toString() + ")");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 检查更新版本
	 */

	public void cheakVersion() {
		LogUtil.d("Wing", "Global.serverVersionCode------>>>>>>>>");
		if (Util.isWifi(mContext)) {
			JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
					Constant.downloadNewApk, null,
					new Response.Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							Message message = mHandler.obtainMessage();
							message.what = Constant.UPDATE_DIALOG_HANDLER;
							message.obj = response;
							mHandler.sendMessage(message);
						}
					}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {

						}
					});
			requestQueue.add(jsonObjectRequest);
		}
	}

	private void upgradeVersion() {
		boolean isUpdate = MySharePreData.GetBooleanTrueData(mContext,
					Constant.UPDATE_DIALOG, newVersionCode + "");
		if (Global.localVersionCode < newVersionCode && isUpdate) {
			LogUtil.i("Wing", "Global.serverVersionCode------>>>>>>>>");
			// 发现新版本，提示用户更新
			final AlertDialog alertDialog = new AlertDialog.Builder(
					MainActivity.this).setCancelable(true).create();
			alertDialog.setCanceledOnTouchOutside(false);
			alertDialog.show();
			Window window = alertDialog.getWindow();
			window.setContentView(R.layout.update_dialog);
			TextView tv_title = (TextView) window.findViewById(R.id.version_title);
			tv_title.setText("钱富通" + newVersionName + "震撼发布");
			TextView tv_UpdateContent_1 = (TextView) window.findViewById(R.id.update_content_1);
			tv_UpdateContent_1.setText("1:" + updateContentDetail[0]);
			TextView tv_UpdateContent_2 = (TextView) window.findViewById(R.id.update_content_2);
			tv_UpdateContent_2.setText("2:" + updateContentDetail[1]);
			TextView tv_UpdateContent_3 = (TextView) window.findViewById(R.id.update_content_3);
			tv_UpdateContent_3.setText("3:" + updateContentDetail[2]);
			ImageButton updateNow = (ImageButton) window.findViewById(R.id.update_now);
			ImageButton updateAfter = (ImageButton) window.findViewById(R.id.update_after);
			updateNow.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this,
							AppUpgradeService.class);
					LogUtil.i(TAG, "updateNow.setOnClickListener");
					intent.putExtra("downloadUrl", downloadUrl);
					startService(intent);
					alertDialog.dismiss();
				}
			});
			updateAfter.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					alertDialog.dismiss();
				}
			});
			CheckBox mUpdate_CB = (CheckBox) window
					.findViewById(R.id.update_checkbox);
			mUpdate_CB
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							MySharePreData.SetBooleanData(mContext,
									Constant.UPDATE_DIALOG,
									newVersionCode + "", !isChecked);
							LogUtil.d(TAG, "isChecked:  " + isChecked);
						}
					});
		} else {
			
		}
	}
	
	/** 
     * 获取错误的信息  
     * @param arg1 
     * @return 
     */  
    private String getErrorInfo(Throwable arg1) {  
        Writer writer = new StringWriter();  
        PrintWriter pw = new PrintWriter(writer);  
        arg1.printStackTrace(pw);  
        Throwable cause = arg1.getCause(); 
        while (cause != null) {  
            cause.printStackTrace(pw);
            cause = cause.getCause();  
        }
        pw.close();  
        String error= writer.toString();  
        return error;  
    }
    
    /*
     * 推送Intent
     * (non-Javadoc)
     * @see android.app.Activity#onNewIntent(android.content.Intent)
     */
    @Override
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
    	if (intent.getAction().equals("com.qianft.m.qian.push")) {
    		setIntent(intent);
        	LogUtil.d(TAG, "intent:   " + intent);
    	}
    }
    
    @SuppressLint("NewApi") 
    private void testEvaluateJavascript(WebView webview) {
    	webview.evaluateJavascript("getSumValue()", new ValueCallback<String>() {
			
			@Override
			public void onReceiveValue(String value) {
				Log.i(TAG, "onReceiveValue value=   " + value);
			}
		});
    }
    
    /*private BroadcastReceiver mScreenBroadcast = new BroadcastReceiver() {
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
                    	 isActive = false;  
                    	 Global.Screen_Off_Flag = false ;
                     }
                     
                 //}
            	 Log.d("Wing", "ACTION_SCREEN_OFF");
             }  
    	};
    };*/
}
