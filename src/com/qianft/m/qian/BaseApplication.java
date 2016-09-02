package com.qianft.m.qian;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.greenrobot.eventbus.EventBus;

import android.Manifest.permission;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.qianft.m.qian.activity.MainActivity;
import com.qianft.m.qian.common.Constant;
import com.qianft.m.qian.common.Global;
import com.qianft.m.qian.view.LockPatternUtils;
import com.umeng.common.message.Log;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;
import com.umeng.socialize.PlatformConfig;


public class BaseApplication extends Application{

	private static BaseApplication mInstance;
	private LockPatternUtils mLockPatternUtils;
	private static final String TAG = BaseApplication.class.getName();
	@Override
	public void onCreate() {
		super.onCreate();
		
		mInstance = this;
		initLocalVersion();
		PushAgent mPushAgent = PushAgent.getInstance(this);
		mPushAgent.setDebugMode(true);
		mPushAgent.setNotificationClickHandler(notificationClickHandler);
		mPushAgent.setMessageHandler(messageHandler);

		mLockPatternUtils = new LockPatternUtils(this);

		Log.d("Wing", "device----->>>>>" + getDeviceInfo(this));
		
		 //微信 appid appsecret
		PlatformConfig.setWeixin(Constant.APP_ID, Constant.APP_SECRET);
		 //新浪微博 appkey appsecret
		PlatformConfig.setSinaWeibo("3921700954","04b48b094faeb16683c32669824ebdad");
		// QQ和Qzone appid appkey 
		PlatformConfig.setQQZone(Constant.QQ_APP_ID, Constant.QQ_APP_SECRET); 

	}
	
	public static BaseApplication getInstance() {
		return mInstance;
	}
	
	public LockPatternUtils getLockPatternUtils() {
		return mLockPatternUtils;
	}
	
	public void initLocalVersion(){
        PackageInfo pinfo;
        ApplicationInfo ainfo;
        try {
            pinfo = this.getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
            Global.localVersionCode = pinfo.versionCode;
            Global.localVersionName = pinfo.versionName;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	UmengMessageHandler messageHandler = new UmengMessageHandler(){
        @Override
        public void dealWithCustomMessage(final Context context, final UMessage msg) {
            new Handler().post(new Runnable() {
                     
                @Override
                public void run() {
                        // TODO Auto-generated method stub
                        // 对自定义消息的处理方式，点击或者忽略
                        boolean isClickOrDismissed = true;
                        if(isClickOrDismissed) {
                        //统计自定义消息的打开
                                UTrack.getInstance(getApplicationContext()).trackMsgClick(msg);
                        } else {
                        	//统计自定义消息的忽略
                                UTrack.getInstance(getApplicationContext()).trackMsgDismissed(msg);
                        }
                        Toast.makeText(context, msg.custom, Toast.LENGTH_LONG).show();
                }
            });
        }
         
        //自定义通知样式
       /* @Override
        public Notification getNotification(Context context,UMessage msg) {
                switch (msg.builder_id) {
                //自定义通知样式编号
                case 1:
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                        RemoteViews myNotificationView = new RemoteViews(context.getPackageName(), R.layout.notification_view);
                        myNotificationView.setTextViewText(R.id.notification_title, msg.title);
                        myNotificationView.setTextViewText(R.id.notification_text, msg.text);
                        myNotificationView.setImageViewBitmap(R.id.notification_large_icon, getLargeIcon(context, msg));
                        myNotificationView.setImageViewResource(R.id.notification_small_icon, getSmallIconId(context, msg));
                        builder.setContent(myNotificationView);
                        builder.setAutoCancel(true);
                        Notification mNotification = builder.build();
                        //由于Android v4包的bug，在2.3及以下系统，Builder创建出来的Notification，并没有设置RemoteView，故需要添加此代码
                        mNotification.contentView = myNotificationView;
                        return mNotification;
                default:
                        //默认为0，若填写的builder_id并不存在，也使用默认。
                        return super.getNotification(context, msg);
                }
        }*/
	};

	/**
	 * 该Handler是在BroadcastReceiver中被调用，故
	 * 如果需启动Activity，需添加Intent.FLAG_ACTIVITY_NEW_TASK
	 * 参考集成文档的1.6.2
	 * [url=http://dev.umeng.com/push/android/integration#1_6_2]http://dev.umeng.com/push/android/integration#1_6_2[/url]
	 * */
	UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler(){
	        //点击通知的自定义行为
			@Override
	        public void dealWithCustomAction(Context context, UMessage msg) {
	               // Toast.makeText(context, msg.custom, Toast.LENGTH_LONG).show();
	                Map<String, String> pushMap = msg.extra;
	                Intent intent = new Intent();
	                intent.setClass(getApplicationContext(), MainActivity.class);
	                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                intent.setAction("com.qianft.m.qian.push");
	                String push_url = pushMap.get("key");
	                EventBus.getDefault().post(push_url);
	                intent.putExtra("Push_Url", push_url);
	                Log.d(TAG, "Push_Url: --------->>>>>>>>>>>>" + push_url);
	                startActivity(intent);
	        }
			
	};


	public static boolean checkPermission(Context context, String permission) {
	    boolean result = false;
	    if (Build.VERSION.SDK_INT >= 23) {
	        try {
	            Class<BaseApplication> clazz = (Class<BaseApplication>) Class.forName("android.content.Context");
	            Method method = clazz.getMethod("checkSelfPermission", String.class);
	            int rest = (Integer) method.invoke(context, permission);
	            if (rest == PackageManager.PERMISSION_GRANTED) {
	                result = true;
	            } else {
	                result = false;
	            }
	        } catch (Exception e) {
	            result = false;
	        }
	    } else {
	        PackageManager pm = context.getPackageManager();
	        if (pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
	            result = true;
	        }
	    }
	    return result;
	}
	public static String getDeviceInfo(Context context) {
	    try {
	        org.json.JSONObject json = new org.json.JSONObject();
	        android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
	                .getSystemService(Context.TELEPHONY_SERVICE);
	        String device_id = null;
	        if (checkPermission(context, permission.READ_PHONE_STATE)) {
	            device_id = tm.getDeviceId();
	        }
	        String mac = null;
	        FileReader fstream = null;
	        try {
	            fstream = new FileReader("/sys/class/net/wlan0/address");
	        } catch (FileNotFoundException e) {
	            fstream = new FileReader("/sys/class/net/eth0/address");
	        }
	        BufferedReader in = null;
	        if (fstream != null) {
	            try {
	                in = new BufferedReader(fstream, 1024);
	                mac = in.readLine();
	            } catch (IOException e) {
	            } finally {
	                if (fstream != null) {
	                    try {
	                        fstream.close();
	                    } catch (IOException e) {
	                        e.printStackTrace();
	                    }
	                }
	                if (in != null) {
	                    try {
	                        in.close();
	                    } catch (IOException e) {
	                        e.printStackTrace();
	                    }
	                }
	            }
	        }
	        json.put("mac", mac);
	        if (TextUtils.isEmpty(device_id)) {
	            device_id = mac;
	        }
	        if (TextUtils.isEmpty(device_id)) {
	            device_id = android.provider.Settings.Secure.getString(context.getContentResolver(),
	                    android.provider.Settings.Secure.ANDROID_ID);
	        }
	        json.put("device_id", device_id);
	        return json.toString();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	private boolean isInLauncher() {
		ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
		String name = manager.getRunningTasks(1).get(0).topActivity.getClassName();
		if(name.equals("com.android.launcher.Launcher")){
		return true;
		}
		return false;
	}
                  
}
