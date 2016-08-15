package com.qianft.m.qian.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class HttpUtils {
	
	public static final int HTTP_RESULT_ACCOUNT_ERROR = 212;
	public static HttpParams httpParams;

	/*RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
	 
	StringRequest stringRequest = new StringRequest(Request.Method.POST,httpurl,
		    new Response.Listener<string>() {
		        @Override
		        public void onResponse(String response) {
		            Log.d(TAG, "response -> " + response);
		        }
		    }, new Response.ErrorListener() {
		        @Override
		        public void onErrorResponse(VolleyError error) {
		            Log.e(TAG, error.getMessage(), error);
		        }
		    }) {
		    @Override
		    protected Map<string, string=""> getParams() {
		        //在这里设置需要post的参数
		              Map<string, string=""> map = new HashMap<string, string="">();  
		            map.put("name1", "value1");  
		            map.put("name2", "value2");  
		 
		          return params;
		    }
		};*/
	
	 /** 
     * @param url 发送请求的URL 
     * @param params 请求参数 
     * @return 服务器响应字符串 
     * @throws Exception 
     */ 
    public static String postRequest(String url, Map<String ,String> rawParams,Context ctx) throws Exception 
    {  
    	HttpClient httpClient = null;
    	
        try{  
        	//创建HttpPost对象。  
            HttpPost post = new HttpPost(url); 
            
            //如果传递参数个数比较多的话可以对传递的参数进行封装  
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            
            for(String key : rawParams.keySet())  
            {  
                //封装请求参数  
                params.add(new BasicNameValuePair(key , rawParams.get(key)));  
            }  
            //设置请求参数 
            post.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));  
            
            httpParams = new BasicHttpParams();
            //设置连接超时和 Socket 超时，以及 Socket 缓存大小
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            HttpConnectionParams.setSoTimeout(httpParams, 10000);
            HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
            
            //设置重定向，缺省为 true
            HttpClientParams.setRedirecting(httpParams, true);

            //设置 user agent
            String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
            HttpProtocolParams.setUserAgent(httpParams, userAgent);

            //创建一个 HttpClient 实例
            //注意 HttpClient httpClient = new HttpClient();是Commons HttpClient
            //中的用法，在 Android 1.5 中我们需要使用 Apache 的缺省实现 DefaultHttpClient
            httpClient = new DefaultHttpClient(httpParams);
            //发送POST请求  
            HttpResponse httpResponse = httpClient.execute(post);  
            //如果服务器成功地返回响应  
            if (httpResponse.getStatusLine().getStatusCode() == 200)  
            {  
                //获取服务器响应字符串  
                String result = EntityUtils.toString(httpResponse.getEntity()); 
                Log.d("Wing", "------====--->>>" + result);
                return result;  
                
            }  
        }catch(Exception e){  
            e.printStackTrace();  
        }finally{  
        	if(null != httpClient && null != httpClient.getConnectionManager())
        		httpClient.getConnectionManager().shutdown();  
        }  
        return null; 
    } 
    
    /*
     * 获取POST请求内容（请求的代码为Map）
     * 参数：
     * context：当前上下文；
     * url：请求的url地址；
     * tag：当前请求的标签；
     * params：POST请求内容；
     * volleyListenerInterface：VolleyListenerInterface接口；
     * */
     /*public static void RequestPost(Context context, String url, String tag, final Map<String, String> params, VolleyListenerInterface volleyListenerInterface) {
         // 清除请求队列中的tag标记请求
         LIMSApplication.getRequestQueue().cancelAll(tag);
         // 创建当前的POST请求，并将请求内容写入Map中
         stringRequest = new StringRequest(Request.Method.POST, url, volleyListenerInterface.responseListener(), volleyListenerInterface.errorListener()){
             @Override
             protected Map<String, String> getParams() throws AuthFailureError {
                 return params;
             }
         };
         // 为当前请求添加标记
         stringRequest.setTag(tag);
         // 将当前请求添加到请求队列中
         LIMSApplication.getRequestQueue().add(stringRequest);
         // 重启当前请求队列
         LIMSApplication.getRequestQueue().start();
     }*/
	
}
