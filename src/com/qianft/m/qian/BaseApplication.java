package com.qianft.m.qian;

import java.util.HashMap;
import java.util.Map;

import com.qianft.m.qian.activity.MainActivity;
import com.qianft.m.qian.common.Global;
import com.umeng.common.message.Log;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

public class BaseApplication extends Application{

	private static final String TAG = BaseApplication.class.getName();
	@Override
	public void onCreate() {
		super.onCreate();
		initLocalVersion();
		PushAgent mPushAgent = PushAgent.getInstance(this);
		mPushAgent.setDebugMode(true);
		mPushAgent.setNotificationClickHandler(notificationClickHandler);
		mPushAgent.setMessageHandler(messageHandler);
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
	                intent.putExtra("Push_Url", push_url);
	                Log.d(TAG, "Push_Url: --------->>>>>>>>>>>>" + push_url);
	                startActivity(intent);
	        }
			
	};
}
