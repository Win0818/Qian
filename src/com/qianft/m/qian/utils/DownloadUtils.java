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

import android.os.Environment;
import android.util.Log;

public class DownloadUtils {
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int DATA_TIMEOUT = 40000;
    private final static int DATA_BUFFER =   1024;//8192;

    public interface DownloadListener {
        public void downloading(int progress);
        public void downloaded();
    }

    @SuppressWarnings("deprecation")/*throws Exception*/
	public static long download(String urlStr, File dest, boolean append, DownloadListener downloadListener)  {
        int downloadProgress = 0;
        long remoteSize = 0;
        int currentSize = 0;
        long totalSize = -1;
        
        LogUtil.i("Wing", "download:   " + urlStr);
        if(!append && dest.exists() && dest.isFile()) {
            dest.delete();
        }

        if(append && dest.exists() && dest.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(dest);
                currentSize = fis.available();
            } catch(IOException e) {
                //throw e;
            	e.printStackTrace();
            	
            } finally {
               
                    try {
                    	 if(fis != null) {
						    fis.close();
                    	 }
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
        } catch (IllegalStateException e) {
        	LogUtil.i("Wing", "download  IllegalStateException---->>>>>>>>>>>");
        }  catch (IOException e) {
        	LogUtil.i("Wing", "download   IOException----------->>>>>>>>>>>");
        }
        
        finally {
            
                try {
                	if(os != null) {
					os.close();
                	}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            
                try {
                	if(is != null) {
					is.close();
                	}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }

        if(totalSize < 0) {
            /*try {
				throw new Exception("Download file fail: " + urlStr);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
        }

       if(downloadListener!= null){
        	LogUtil.i("Wing", "downloadListener------------>>>>>>>>>>>");
            downloadListener.downloaded();
        }
        return totalSize;
    }
    
    public static void download_2(String urlStr,  boolean append,  File dest,  DownloadListener downloadListener) {
				int downloadProgress = 0;
		        long remoteSize = 0;
		        int currentSize = 0;
		        long totalSize = -1;
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					URL url ;
					try {
						url = new URL("http://gdown.baidu.com/data/wisegame/79fb2f638cc11043/oldoffender.apk");
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
						 conn .setRequestProperty("Accept-Encoding", "identity"); 
						conn.setConnectTimeout(5000);
						conn.connect();
						// 获取到文件的大小
						int length = conn.getContentLength();
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
		                        Log.i("Wing","------下载进度------" + String.valueOf(downloadProgress));
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
    
   
}
