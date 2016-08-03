package com.qianft.m.qian.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
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
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
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
import com.qianft.m.qian.R;
import com.qianft.m.qian.common.Constant;
import com.qianft.m.qian.common.Global;
import com.qianft.m.qian.service.AppUpgradeService;
import com.qianft.m.qian.utils.LogUtil;
import com.qianft.m.qian.utils.MySharePreData;
import com.qianft.m.qian.utils.SharePopMenu;
import com.qianft.m.qian.utils.SharePopMenu.shareBottomClickListener;
import com.qianft.m.qian.utils.Util;
import com.qianft.m.qian.view.GlobalProgressDialog;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * 
 * @author Administrator 只要在网站的目录下配置一个扩展名为： .appcache 的 Manifest 文件，注明哪些文件需缓存，
 *         哪些文件必须经过网络去加载，然后在 <html> 标签中加入 <html manifest="demo.appcache">
 *         即可完成缓存的实现。
 */
public class MainActivity extends Activity implements OnClickListener,
		shareBottomClickListener {

	private String TAG = "Wing";
	private WebView mWebView;
	private ImageButton mRefreshBtn;
	private LinearLayout mNoNetworkLinearLayout;
	private boolean DEBUG = true;
	private String mAddress = Constant.Address;
	// private String mAddress = "file:///android_asset/html/index.html";
	// private String mAddress = "http://192.168.0.70:8088/Home/Index";
	private String mShareUrl;
	private String mTitle;
	private Bitmap mIcom;
	private long exitTime = 0;
	private String mNowUrl = "http://m.qianft.com/";
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
	private String updateContent = null;
	private int newVersionCode = 1;
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
					updateContent = jsonObject.getString("Update_Content");
					LogUtil.d("Wing", "response:  " + jsonObject.toString()
							+ "-----" + downloadUrl + "-----" + newVersionName
							+ "-----" + newVersionCode + "-----" + packageSize
							+ "-----" + updateContent);
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
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
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
		popMenu = new SharePopMenu(this);
		/*
		 * popMenu.addItem(new ShareObject(getResources().getString(
		 * R.string.share_to_wechat_freind), R.drawable.share_to_freind));
		 * popMenu.addItem(new ShareObject(getResources().getString(
		 * R.string.share_to_wechat_circle), R.drawable.share_to_circle));
		 */
		popMenu.setShareBottomClickListener(this);
	}

	private void initData() {
		EventBus.getDefault().register(this);
		requestQueue = Volley.newRequestQueue(mContext);
		cheakVersion();
	}

	/**
	 * 当Activity执行onResume()时让WebView执行resume
	 */
	@Override
	protected void onResume() {
		super.onResume();
		LogUtil.d(TAG, "onResume:  " + Util.WECHAT_CODE);
	}

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
		if (mShareUrl != null && mTitle != null && mDescription != null
				&& mImageUrl != null) {
			share(Constant.SHARE_TO_FREIND_CIRCLE, mShareUrl, mTitle,
					mDescription, mImageUrl);
			LogUtil.d("Wing", "mShareUrl: " + mShareUrl + "mTitle: " + mTitle
					+ "mDescription: " + mDescription + "mImageUrl: "
					+ mImageUrl);
		}
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
			webSettings.setAllowFileAccess(false);
			webSettings.setUseWideViewPort(false);
			webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
			webSettings.setBlockNetworkImage(true);
			webSettings.setSavePassword(false);
			/*
			 * if (Build.VERSION.SDK_INT <= 18) {
			 * webSettings.setSavePassword(false); } else { // do nothing.
			 * because as google mentioned in the documentation - //
			 * "Saving passwords in WebView will not be supported in future versions"
			 * }
			 */
			mWebView.addJavascriptInterface(getHtmlObject(), "jsObj");
			mWebView.loadUrl(mAddress);
			mWebView.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK && mNowUrl != null
							&& mNowUrl.equals("http://m.qianft.com/")) {

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
						Log.e("Wing", "..shouldOverrideUrlLoading.. url=" + url);
					mNowUrl = url;
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
					view.stopLoading();
					view.clearView();
					LogUtil.d("Wing", "onReceivedError---errorCode---->>>>>>>"
							+ errorCode);
					mHandler.sendEmptyMessage(Constant.NO_NETWORK_HANDLER);
					mNoNetworkLinearLayout.setVisibility(View.VISIBLE);
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
						// mNoNetworkLinearLayout.setVisibility(View.INVISIBLE);
					}
					super.onProgressChanged(view, newProgress);
				}

				@Override
				public boolean onJsAlert(WebView view, String url,
						String message, JsResult result) {
					return super.onJsAlert(view, url, message, result);
				}

				@Override
				public boolean onJsConfirm(WebView view, String url,
						String message, JsResult result) {
					return super.onJsConfirm(view, url, message, result);
				}
			});

		} catch (Exception e) {
			return;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		// if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
		// mWebView.goBack(); return false; } else
		if (keyCode == KeyEvent.KEYCODE_BACK /* && !mWebView.canGoBack() */) {
			exitApp();
			return false;
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
	private Object getHtmlObject() {

		Object insertObj = new Object() {
			@JavascriptInterface
			public void share_To_Wechat_android(String webpageUrl,
					String title, String description, String imageUrl) {
				mTitle = title;
				mShareUrl = webpageUrl;
				mDescription = description;
				mImageUrl = imageUrl;
				popMenu.showAsDropDown(MainActivity.this
						.findViewById(R.id.main_root));
			}

			@JavascriptInterface
			public String loginWechat_android() {

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						String uid = MySharePreData.GetData(MainActivity.this,
								Constant.WECHAT_LOGIN_SP_NAME, "union_id");
						if (TextUtils.isEmpty(uid)) {
							loginWechat_2();
						} else {
							Log.d(TAG, "union_id:   " + uid);
							mWebView.loadUrl("http://m.qianft.com/UserLogin/WeChatLogin?unionId=UNIONID"
									.replace("UNIONID", uid));
						}
					}
				});
				return "login succeed";
				// }
			}

			@JavascriptInterface
			public void takePhoto_android(String path, String picFileName) {
				takePhoto(path, picFileName);
			}

			// 微信授权
			@JavascriptInterface
			public String wechat_Auth_Login_android(String userid) {
				Log.d("Wing", "userid:   " + userid);
				Util.USER_ID = userid;
				loginWechat();
				return "";
			}

			@JavascriptInterface
			public String HtmlcallJava2(final String param) {
				String uid = MySharePreData.GetData(mContext,
						Constant.WECHAT_LOGIN_SP_NAME, "union_id");
				if (uid.equals("")) {
					uid = "请授权微信登录";
				}
				return "Union_Id:  " + uid;
			}

			@JavascriptInterface
			public void JavacallHtml() {
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

			// 以json实现webview与js之间的数据交互
			public String jsontohtml() {
				JSONObject map;
				JSONArray array = new JSONArray();
				try {
					map = new JSONObject();
					map.put("name", "aaron");
					map.put("age", 25);
					map.put("address", "中国上海");
					array.put(map);

					map = new JSONObject();
					map.put("name", "jacky");
					map.put("age", 22);
					map.put("address", "中国北京");
					array.put(map);

					map = new JSONObject();

					map.put("name", "vans");
					map.put("age", 26);
					map.put("address", "中国深圳");
					map.put("phone", "13888888888");
					array.put(map);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return array.toString();
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
			@JavascriptInterface
			public void downloadPicture(final String imageUrl,
					final String savePath, final String picFileName) {
				Util.downLoadPicture(imageUrl, savePath, picFileName);
			}

			@JavascriptInterface
			public void clearUserInfo_android() {
				MySharePreData.SetData(mContext, Constant.WECHAT_LOGIN_SP_NAME,
						"union_id", "");
			}

			@JavascriptInterface
			public void startQQDialog_android(String qqId) {
				String url = "mqqwpa://im/chat?chat_type=wpa&uin=UIN".replace(
						"UIN", qqId);
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
			}
		};

		return insertObj;
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

	public void sendInfoToJs(View view) {
		String msg1 = ((EditText) findViewById(R.id.input_et)).getText()
				.toString();
		String msg2 = "I am wuyong";
		// 调用js中的函数：showInfoFromJava(msg)
		String openId = "openId------->";
		String uid = "uid---------->";
		mWebView.loadUrl("javascript:showInfoFromJava('" + msg1 + "','"
				+ openId + "','" + uid + "')");
		// mWebView.loadUrl("javascript:showInfoFromJava2('" + msg1 + "','" +
		// msg2 + "')");

		String call = "javascript:alertMessage(\"" + "content" + "\")";
		mWebView.loadUrl(call);
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
		startActivityForResult(intent, TAKE_PHOTO);
	}

	@Override
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
	}

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
	protected void onDestroy() {
		super.onDestroy();
		removeAllCookie();
		EventBus.getDefault().unregister(this);
	}
	/**
	 * EventBus2.0 微信授权
	 * @param message
	 */

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void helloEventBus(String message) {
		Log.d("Wing", "message；  " + message);
		if (message.equals("hello")) {
			mWebView.loadUrl("http://m.qianft.com/WeiXin/Success");
		} else if (message.equals("login_state")) {
			String uid = MySharePreData.GetData(MainActivity.this,
					Constant.WECHAT_LOGIN_SP_NAME, "union_id");
			mWebView.loadUrl("http://m.qianft.com/UserLogin/WeChatLogin?unionId=UNIONID"
					.replace("UNIONID", uid));
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
			LogUtil.d("Wing", "Global.serverVersionCode------>>>>>>>>");
			// 发现新版本，提示用户更新
			final AlertDialog alertDialog = new AlertDialog.Builder(
					MainActivity.this).setCancelable(true).create();
			alertDialog.setCanceledOnTouchOutside(false);
			alertDialog.show();
			Window window = alertDialog.getWindow();
			window.setContentView(R.layout.update_dialog);
			TextView tv_title = (TextView) window.findViewById(R.id.version_name);
			tv_title.setText("版本号： " + newVersionName);
			TextView tv_message = (TextView) window.findViewById(R.id.package_size);
			tv_message.setText("安装包大小： " + packageSize);
			TextView tv_UpdateContent = (TextView) window.findViewById(R.id.update_message);
			tv_UpdateContent.setText("更新内容： " + updateContent);
			Button updateNow = (Button) window.findViewById(R.id.update_now);
			Button updateAfter = (Button) window.findViewById(R.id.update_after);

			updateNow.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this,
							AppUpgradeService.class);
					intent.putExtra("downloadUrl", Constant.mLatestVersionDownload);
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

}
