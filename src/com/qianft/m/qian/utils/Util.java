package com.qianft.m.qian.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;


public class Util {
	private String TAG = "Wing";
	public static String WECHAT_CODE = null;
	private IWXAPI wxApi;
	public static String USER_ID = null;
	public static String COOKIES = null;
	
	/**
	 * Bitmap 到字节数组
	 * @param bmp
	 * @param needRecycle
	 * @return
	 */
	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}
		
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	private  void share(int flag,String url,  String title, String description, Bitmap thumb){  
	    WXWebpageObject webpage = new WXWebpageObject();  
	    //webpage.webpageUrl = "http://m.qianft.com/Team/Index";
	    webpage.webpageUrl = url;
	    WXMediaMessage msg = new WXMediaMessage(webpage);  
	    msg.title = title;  
	    msg.description = description; 
	    
	    //Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.qianfutong);  
	    msg.setThumbImage(thumb);  
	      
	    SendMessageToWX.Req req = new SendMessageToWX.Req();  
	    req.transaction =buildTransaction("webpage"); 
	    req.message = msg;  
	    req.scene = flag==0?SendMessageToWX.Req.WXSceneSession:SendMessageToWX.Req.WXSceneTimeline;  
	    boolean fla = wxApi.sendReq(req);  
	    System.out.println("fla="+fla);
	}  
	
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
	/**
	 * 下载图片到本地
	 * @param imageUrl
	 * @param savePath
	 * @param picFileName
	 */
	public static void downLoadPicture(final String imageUrl, final String savePath,final String picFileName) {
		
		new Thread() {
			@Override
			public void run() {
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					URL url ;
					try {
						url = new URL(imageUrl);
						Log.d("Wing", "=------picture---URL---"
								+ imageUrl);
						String rootPath = Environment.getExternalStorageDirectory().toString();
						File pathDir = new File(rootPath + savePath);
						if (!pathDir.exists()) {
							pathDir.mkdirs();
						}
						File outputImage = new File(pathDir,
								picFileName);
						try {
							if (outputImage.exists()) {
								outputImage.delete();
							}
							outputImage.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
						HttpURLConnection conn = (HttpURLConnection) url
								.openConnection();
						conn.setConnectTimeout(5000);
						// 获取到文件的大小
						InputStream is = conn.getInputStream();
						
						/*File updatefile = new File(
						Environment.getExternalStorageDirectory()+ "/" + savePath +"/"+ picFileName + ".jpg");
						if (updatefile.exists()) {
							updatefile.delete();
							updatefile.createNewFile();
						} else {
							updatefile.createNewFile();
						}*/
						FileOutputStream fos = new FileOutputStream(outputImage);

						BufferedInputStream bis = new BufferedInputStream(is);
						byte[] buffer = new byte[1024];
						int len;
						while ((len = bis.read(buffer)) != -1 ) {
							fos.write(buffer, 0, len);
						}
						fos.close();
						bis.close();
						is.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	/**
	 * 检测网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetWorkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}
	
	/** 
	 * make true current connect service is wifi 
	 * @param mContext 
	 * @return 
	 */  
	public static boolean isWifi(Context mContext) {  
	    ConnectivityManager connectivityManager = (ConnectivityManager) mContext  
	            .getSystemService(Context.CONNECTIVITY_SERVICE);  
	    NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();  
	    if (activeNetInfo != null  
	            && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {  
	        return true;  
	    }  
	    return false;  
	}  
	
}
