package com.qianft.m.qian.wxapi;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.qianft.m.qian.common.Constant;
import com.qianft.m.qian.utils.HttpUtils;
import com.qianft.m.qian.utils.JsonUtils;
import com.qianft.m.qian.utils.LogUtil;
import com.qianft.m.qian.utils.MySharePreData;
import com.qianft.m.qian.utils.Util;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth.Resp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
	// IWXAPI 是第三方app和微信通信的openapi接口
	private String TAG = "Wing";
	private IWXAPI api;
	private Context mContext;
	private static HashMap<String, String> userInfoMap;
	public static final String PostUserInfoUrl = "http://m.qianft.com/WeiXin/SaveUserAuthenInfo";
	private String state;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constant.RETURN_OPENID_ACCESSTOKEN:
				Bundle bundle1 = (Bundle) msg.obj;
				String accessToken = bundle1.getString("access_token");
				String openId = bundle1.getString("openid");
				MySharePreData.SetData(mContext, Constant.WECHAT_LOGIN_SP_NAME,
						"openid", openId);
				getUID(openId, accessToken);
				break;
			case Constant.RETURN_NICKNAME_UID:
				Bundle bundle2 = (Bundle) msg.obj;
				String uid = bundle2.getString("unionid");
				MySharePreData.SetData(mContext, Constant.WECHAT_LOGIN_SP_NAME,
						"union_id", uid);
				if (state.equals("login_state")) {
					EventBus.getDefault().post("login_state");
				} else if (state.equals("wechat_sdk_demo_test")) {
					EventBus.getDefault().post("hello");
				}
				WXEntryActivity.this.finish();
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		api = WXAPIFactory.createWXAPI(this, Constant.APP_ID, false);
		api.handleIntent(getIntent(), this);
		mContext = this;
	}

	@Override
	public void onReq(BaseReq arg0) {
	}

	@Override
	public void onResp(BaseResp resp) {
		// LogManager.show("Wing", "resp.errCode:" + resp.errCode +
		// ",resp.errStr:"
		// + resp.errStr, 1);
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			Log.i(TAG, "onResp");
			if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
				Resp sendAuthResp = (Resp) resp;// 用于分享时不要有这个，不能强转
				String code = sendAuthResp.code;
				state = sendAuthResp.state;
				Util.WECHAT_CODE = code;
				Log.d(TAG, "code:  " + code + "state:  " + state);
				int errCode = resp.errCode;
				getResult(code);
			} else if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
				Toast.makeText(getApplicationContext(), "分享成功", 2000).show();
				this.finish();
				// 分享成功
			}
			// 可用以下两种方法获得code
			// resp.toBundle(bundle);
			// Resp sp = new Resp(bundle);
			// String code = sp.code;<span style="white-space:pre">
			// 或者
			// String code = ((SendAuth.Resp) resp).(BaseResp.ErrCode.ERR_OK);
			// 上面的code就是接入指南里要拿到的code

			System.out.println("success");
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
				Toast.makeText(getApplicationContext(), "登录取消", 2000).show();
				System.out.println("ERR_USER_CANCEL");
			} else if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
				// 分享取消
				Toast.makeText(getApplicationContext(), "分享取消", 2000).show();
				
				System.out.println("ERR_USER_CANCEL");
			}
			this.finish();
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
				Toast.makeText(getApplicationContext(), "授权成功", 2000).show();
				System.out.println("ERR_USER_CANCEL");
			} else if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
				// 分享拒绝
				Toast.makeText(getApplicationContext(), "分享成功", 2000).show();
				System.out.println("ERR_USER_CANCEL");
			}
			this.finish();
			break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(intent, this);
		finish();
	}

	/**
	 * 获取openid accessToken值用于后期操作
	 * 
	 * @param code请求码
	 */
	private void getResult(final String code) {
		new Thread() {// 开启工作线程进行网络请求
			public void run() {
				String path = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
						+ Constant.APP_ID
						+ "&secret="
						+ Constant.APP_SECRET
						+ "&code=" + code + "&grant_type=authorization_code";
				try {
					JSONObject jsonObject = JsonUtils
							.initSSLWithHttpClinet(path);// 请求https连接并得到json结果
					if (null != jsonObject) {
						String openid = jsonObject.getString("openid")
								.toString().trim();
						String access_token = jsonObject
								.getString("access_token").toString().trim();
						Log.i(TAG, "getResult  openid ======= " + openid);
						Log.i(TAG, "access_token = " + access_token);

						Message msg = mHandler.obtainMessage();
						msg.what = Constant.RETURN_OPENID_ACCESSTOKEN;
						Bundle bundle = new Bundle();
						bundle.putString("openid", openid);
						bundle.putString("access_token", access_token);
						msg.obj = bundle;
						mHandler.sendMessage(msg);
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return;
			};
		}.start();
	}

	/**
	 * 获取用户唯一标识
	 * 
	 * @param openId
	 * @param accessToken
	 */
	private void getUID(final String openId, final String accessToken) {
		new Thread() {
			@Override
			public void run() {
				Log.d(TAG, "getUID-------->>>>>>>");
				String path = "https://api.weixin.qq.com/sns/userinfo?access_token="
						+ accessToken + "&openid=" + openId;
				JSONObject jsonObject = null;
				try {
					jsonObject = JsonUtils.initSSLWithHttpClinet(path);
					String nickname = jsonObject.getString("nickname");
					String unionid = jsonObject.getString("unionid");
					String headimgurl = jsonObject.getString("headimgurl");
					String openid = jsonObject.getString("openid");
					String sex = jsonObject.getString("sex");
					String province = jsonObject.getString("province");
					String city = jsonObject.getString("city");
					String country = jsonObject.getString("country");
					String userid = Util.USER_ID; // Util.USER_ID;
					LogUtil.i(TAG, "nickname = " + nickname);
					LogUtil.i(TAG, "unionid = " + unionid);
					LogUtil.i(TAG, "headimgurl = " + headimgurl + "---" + province
							+ "---" + city + "---" + sex + "---" + country
							+ "---" + userid);
					userInfoMap = new HashMap<String, String>();
					userInfoMap.put("headimgurl", headimgurl);
					userInfoMap.put("openid", openid);
					userInfoMap.put("nickname", nickname);
					userInfoMap.put("sex", sex);
					userInfoMap.put("province", province);
					userInfoMap.put("city", city);
					userInfoMap.put("contry", country);
					userInfoMap.put("unionid", unionid);
					userInfoMap.put("userid", userid);
					
					if (state.equals("wechat_sdk_demo_test")) {
						String result = HttpUtils.postRequest(PostUserInfoUrl,
								userInfoMap, WXEntryActivity.this);
						LogUtil.d("Wing", "--post commit---" + result);
					}
					Message msg = mHandler.obtainMessage();
					msg.what = Constant.RETURN_NICKNAME_UID;
					Bundle bundle = new Bundle();
					bundle.putString("nickname", nickname);
					bundle.putString("unionid", unionid);
					bundle.putString("headimgurl", headimgurl);
					bundle.putString("openid", openid);
					bundle.putString("sex", sex);
					bundle.putString("province", province);
					bundle.putString("city", city);
					bundle.putString("country", country);

					msg.obj = bundle;
					mHandler.sendMessage(msg);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}.start();
	}
}