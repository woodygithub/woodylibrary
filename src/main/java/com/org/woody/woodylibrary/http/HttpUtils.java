package com.org.woody.woodylibrary.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

/**
 * 方便使用http请求的工具类
 */
public class HttpUtils {
	static private DefaultHttpClient httpClient;
    private static final int DEFAULT_MAX_CONNECTIONS = 30;//最大连接数
    private static final int DEFAULT_SOCKET_TIMEOUT = 10 * 1000;
    private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;
    
    private static final int DEFAULT_TRY_TIME =2;
    private static final int DEFAULT_RETRY_DELAY =1000;
    static boolean DEBUG = false;//TODO 修改这里处理Log
	public static synchronized DefaultHttpClient getHttpClient() {  
	    if(httpClient == null) {  
	        final HttpParams httpParams = new BasicHttpParams();    
	          
	        // timeout: get connections from connection pool  
	        ConnManagerParams.setTimeout(httpParams, 1000);    
	        // timeout: connect to the server  
	        HttpConnectionParams.setConnectionTimeout(httpParams, DEFAULT_SOCKET_TIMEOUT);  
	        // timeout: transfer data from server  
	        HttpConnectionParams.setSoTimeout(httpParams, DEFAULT_SOCKET_TIMEOUT);   
	          
	        // set max connections per host  
	        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(10));
	        // set max total connections  
	        ConnManagerParams.setMaxTotalConnections(httpParams, DEFAULT_MAX_CONNECTIONS);  
	          
	        // use expect-continue handshake  
	        HttpProtocolParams.setUseExpectContinue(httpParams, true);  
	        // disable stale check  
	        HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);  
	          
	        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);    
	        HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);   
	            
//	        HttpClientParams.setRedirecting(httpParams, false);  
	          
	        // set user agent  
	        String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";  
	        HttpProtocolParams.setUserAgent(httpParams, userAgent);
	          
	        // disable Nagle algorithm  
	        HttpConnectionParams.setTcpNoDelay(httpParams, true);   
	          
	        HttpConnectionParams.setSocketBufferSize(httpParams, DEFAULT_SOCKET_BUFFER_SIZE);    
	          
	        // scheme: http and https  
	        SchemeRegistry schemeRegistry = new SchemeRegistry();    
	        schemeRegistry.register(new Scheme("http", new PlainSocketFactory(), 80));    
//	        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));  
	  
	        ClientConnectionManager manager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);    

	        httpClient = new DefaultHttpClient(manager, httpParams);  
			DefaultHttpRequestRetryHandler handler = new DefaultHttpRequestRetryHandler(3,true); 
			httpClient.setHttpRequestRetryHandler(handler);  
	    }         
	    return httpClient;  
	}  
	/**使用WebView的Cookie机制 */
	public static void setWebViewCookieStore(Context context){
		getHttpClient().setCookieStore(new WebviewCookieStore(context));
	}
	public static void clearCookies(){
		getHttpClient().getCookieStore().clear();
	}
	public static int reqStatusCodeForPost(String postUrl,Map<String,ContentBody> map){
        HttpRequestBase httpPost = RequestFactory.createPost(postUrl,map);
		return executeForStatusCode(httpPost);
	}
	public static String reqForPost(String postUrl,Map<String,ContentBody> map){
        HttpRequestBase httpPost = RequestFactory.createPost(postUrl,map);
		return executeForString(httpPost);
	}
	public static String reqForPost(String postUrl, NameValuePair... pairs){
        HttpRequestBase httpPost = RequestFactory.createPost(postUrl, pairs);
        return executeForString(httpPost);
	}
	public static String reqForPost(String postUrl, List<NameValuePair> params) {
        HttpRequestBase httpPost = RequestFactory.createPost(postUrl, params);
        return executeForString(httpPost);
    }
	public static String reqForPut(String postUrl) {
        HttpRequestBase httpPost = RequestFactory.createPost(postUrl);
        return executeForString(httpPost);
	}
    public static String reqForGet(String getURL) {
        HttpRequestBase httpGet = RequestFactory.createGet(getURL);
        return executeForString(httpGet);
    }
    /**返回code，请求异常为-1 */
	public static int executeForStatusCode(HttpRequestBase httpRequestBase){
		for(int i=0;i< DEFAULT_TRY_TIME;i++){
			try {
				return executeForStatusCodeImp(httpRequestBase);
			} catch (Exception e) {
				if(DEBUG){
					Log.e("fax", "execute error");
					e.printStackTrace();
				}
                try {
                    Thread.sleep(DEFAULT_RETRY_DELAY);
                } catch (InterruptedException e2) {
                    return -1;//break
                }
			}
		}
		return -1;
	}
	private static int executeForStatusCodeImp(HttpRequestBase httpRequestBase) throws Exception{
			int code = getHttpClient().execute(httpRequestBase).getStatusLine().getStatusCode();
			if(DEBUG) Log.d("fax", "execute Code:" + code);
			return code;
	}
	public static String executeForString(HttpRequestBase httpRequest){
        StringResponse response = execute(httpRequest);
        if(response!=null) return response.getContent();
		return null;
	}
    public static StringResponse execute(HttpRequestBase httpRequest){
        for(int i=0;i< DEFAULT_TRY_TIME;i++){
            try {
				return executeImp(httpRequest);
            } catch (Exception e) {
                if(DEBUG){
                    Log.e("fax", "execute error");
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(DEFAULT_RETRY_DELAY);
                } catch (InterruptedException e2) {
                    return null;//break
                }
            }
        }
        return null;
    }
	@SuppressLint("DefaultLocale")
	private static StringResponse executeImp(HttpRequestBase httpRequest) throws Exception{
			httpRequest.addHeader("Accept-Encoding", "gzip");
			HttpResponse httpResponse = getHttpClient().execute(httpRequest);
			
			InputStream is = httpResponse.getEntity().getContent();
			try {
				String encoding = httpResponse.getEntity().getContentEncoding().getValue().toLowerCase();
				if (encoding.equals("gzip")) is = new GZIPInputStream(is);
			} catch (Exception e) {
			}
			String strResult = readInputStream(new InputStreamReader(is));
            int code = httpResponse.getStatusLine().getStatusCode();
			if (code != HttpStatus.SC_OK && DEBUG ) {
				Log.d("fax", "execute may Fail,Code:" + httpResponse.getStatusLine().getStatusCode()+",Entity:"+strResult);
			}
			httpRequest.abort();
			return new StringResponse(strResult, code);
	}
	/**
	 *  * 从输入流中读入数据  *   * @param request  * @param response  * @param s  
	 */
	public static String readInputStream(InputStreamReader in) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(in);
			char[] temp=new char[1024];
			int length;
			while ((length = reader.read(temp)) != -1) {
				if(Thread.currentThread().isInterrupted()) break;
				sb.append(temp,0,length);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				in.close();
			} catch (IOException e) {
			}
		}
		return (sb.toString());
	}
	
	public static File reqForDownload(HttpRequestBase httpRequest, File file, boolean continueDown, DownloadListener listener){
		try {
			long loaded = 0;
			if(continueDown && file.isFile()){//断点续传
                if(file.length()>0){
    				loaded = file.length();
                	httpRequest.setHeader("Range", "bytes="+file.length()+"-");
                }
			}
			
			HttpResponse httpResponse = getHttpClient().execute(httpRequest);
			int code = httpResponse.getStatusLine().getStatusCode();
			if (code != HttpStatus.SC_OK && DEBUG) {
				Log.d("fax", "execute may Fail,Code:" + httpResponse.getStatusLine().getStatusCode() );
			}
			HttpEntity entity = httpResponse.getEntity();
			InputStream is = entity.getContent();
			try {
				FileOutputStream fos = new FileOutputStream(file, continueDown);
				byte[] temp=new byte[1024];
				int length;
				long total = loaded + entity.getContentLength();
				
				while ((length = is.read(temp)) != -1) {
					if(Thread.currentThread().isInterrupted()) break;
					fos.write(temp, 0, length);
					loaded+=length;
					if(listener!=null) listener.onDownloading(loaded, total);
				}
				fos.close();
				if(listener!=null) listener.onDownloadFinish(file);
				return file;
			} catch (Exception e) {
				e.printStackTrace();
				if(listener!= null) listener.onDownloadError(e.getMessage());
			}finally{
				try {
					is.close();
				} catch (IOException e) {
				}
			}
			
			httpRequest.abort();
		} catch (Exception e) {
			if(listener!= null) listener.onDownloadError(e.getMessage());
		}
		return null;
    }
	public interface DownloadListener{
		public void onDownloadFinish(File file);
		public void onDownloading(long loaded, long total);
		public void onDownloadError(String error);
	}
	
}
