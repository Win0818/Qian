package com.qianft.m.qian.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.qianft.m.qian.common.Global;

import android.app.ProgressDialog;
import android.os.Environment;
import android.util.Log;

public class DownloadUtils {
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int DATA_TIMEOUT = 40000;
    private final static int DATA_BUFFER = 8192;

    public interface DownloadListener {
        public void downloading(int progress);
        public void downloaded();
    }

    public static long download(String urlStr, File dest, boolean append, DownloadListener downloadListener)  throws Exception{
        int downloadProgress = 0;
        long remoteSize = 0;
        int currentSize = 0;
        long totalSize = -1;
        
        LogUtil.d("Wing", "download:   " + urlStr);
        if(!append && dest.exists() && dest.isFile()) {
            dest.delete();
        }

        if(append && dest.exists() && dest.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(dest);
                currentSize = fis.available();
            } catch(IOException e) {
                throw e;
            } finally {
                if(fis != null) {
                    fis.close();
                }
            }
        }

        HttpGet request = new HttpGet(urlStr);

        if(currentSize > 0) {
            request.addHeader("RANGE", "bytes=" + currentSize + "-");
        }

        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, DATA_TIMEOUT);
        HttpClient httpClient = new DefaultHttpClient(params);

        InputStream is = null;
        FileOutputStream os = null;
        try {
            HttpResponse response = httpClient.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                is = response.getEntity().getContent();
                remoteSize = response.getEntity().getContentLength();
                Header contentEncoding = response.getFirstHeader("Content-Encoding");
                if(contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                    is = new GZIPInputStream(is);
                }
                os = new FileOutputStream(dest, append);
                byte buffer[] = new byte[DATA_BUFFER];
                int readSize = 0;
                while((readSize = is.read(buffer)) > 0){
                    os.write(buffer, 0, readSize);
                    os.flush();
                    totalSize += readSize;
                    if(downloadListener!= null){
                        downloadProgress = (int) (totalSize*100/remoteSize);
                        downloadListener.downloading(downloadProgress);
                    }
                }
                if(totalSize < 0) {
                    totalSize = 0;
                }
            }
        } finally {
            if(os != null) {
                os.close();
            }
            if(is != null) {
                is.close();
            }
        }

        if(totalSize < 0) {
            throw new Exception("Download file fail: " + urlStr);
        }

       if(downloadListener!= null){
        	LogUtil.d("Wing", "downloadListener------------>>>>>>>>>>>");
            downloadListener.downloaded();
        }
        return totalSize;
    }
    
    public static void download_2(final String urlStr, final File dest, final DownloadListener downloadListener) {
		
		new Thread() {
			
			@Override
			public void run() {
				int downloadProgress = 0;
		        long remoteSize = 0;
		        int currentSize = 0;
		        long totalSize = -1;
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					URL url ;
					try {
						url = new URL(urlStr);
						Log.i("Wing", "=------picture---URL---"
								+ urlStr);
						String rootPath = Environment.getExternalStorageDirectory().toString();
						/*File pathDir = new File(rootPath + savePath);
						if (!pathDir.exists()) {
							pathDir.mkdirs();
						}*/
						//File outputImage = new File(pathDir,
						//		picFileName);
						/*try {
							if (outputImage.exists()) {
								outputImage.delete();
							}
							outputImage.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}*/
						HttpURLConnection conn = (HttpURLConnection) url
								.openConnection();
						conn.setConnectTimeout(5000);
						// 获取到文件的大小
						conn.getContentLength();
						InputStream is = conn.getInputStream();
						
						/*File updatefile = new File(
						Environment.getExternalStorageDirectory()+ "/" + savePath +"/"+ picFileName + ".jpg");
						if (updatefile.exists()) {
							updatefile.delete();
							updatefile.createNewFile();
						} else {
							updatefile.createNewFile();
						}*/
						FileOutputStream fos = new FileOutputStream(dest);

						BufferedInputStream bis = new BufferedInputStream(is);
						byte[] buffer = new byte[1024];
						int len;
						while ((len = bis.read(buffer)) != -1 ) {
							fos.write(buffer, 0, len);
							totalSize += len;
		                    if(downloadListener!= null){
		                        downloadProgress = (int) (totalSize*100/remoteSize);
		                        downloadListener.downloading(downloadProgress);
		                    }
						}
						fos.close();
						bis.close();
						is.close();
					} catch (Exception e) {
						e.printStackTrace();
					}finally {
						
					}
					if(downloadListener!= null){
			        	LogUtil.d("Wing", "downloadListener------------>>>>>>>>>>>");
			            downloadListener.downloaded();
			        }
				}
			}
		}.start();
	}
    
   
}
