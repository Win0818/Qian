package com.qianft.m.qian;

import com.qianft.m.qian.common.Global;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class BaseApplication extends Application{

	@Override
	public void onCreate() {
		super.onCreate();
		initLocalVersion();
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
}
