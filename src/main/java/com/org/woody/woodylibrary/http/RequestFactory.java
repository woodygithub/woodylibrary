package com.org.woody.woodylibrary.http;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.protocol.HTTP;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by linfaxin on 2014/7/15 015.
 * Email: linlinfaxin@163.com
 * Request构造类
 */
public class RequestFactory {
    private static boolean DEBUG=HttpUtils.DEBUG;

    public static HttpRequestBase createGet(String url){
        if(DEBUG) Log.d("fax", "createGet:"+url);
        return new HttpGet(url);
    }
    public static HttpRequestBase createPost(String url){
        if(DEBUG) Log.d("fax", "createPost:"+url);
        return new HttpPost(url);
    }
    public static HttpRequestBase createPut(String url){
        if(DEBUG) Log.d("fax", "createPut:"+url);
        return new HttpPut(url);
    }
    public static HttpRequestBase createDelete(String url){
        if(DEBUG) Log.d("fax", "createDelete:"+url);
        return new HttpDelete(url);
    }
    public static HttpRequestBase createPost(String postUrl,Map<String,ContentBody> map){
        if(DEBUG) Log.d("fax", "createPost:" + postUrl);
        HttpPost httppost = new HttpPost(postUrl);
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        for(Map.Entry<String, ContentBody> entry:map.entrySet()){
            mpEntity.addPart(entry.getKey(), entry.getValue());
        }
        httppost.setEntity(mpEntity);
        return httppost;
    }
    public static HttpRequestBase createPost(String postUrl, NameValuePair... pairs){
        List<NameValuePair> pairList= Arrays.asList(pairs);
        return createPost(postUrl, pairList);
    }
    public static HttpRequestBase createPost(String postURL, List<NameValuePair> params) {
        if(DEBUG) Log.d("fax", "createPost:" + postURL);
        if(DEBUG) for(NameValuePair pair:params){
            Log.d("fax", pair.getName()+":"+pair.getValue());
        }
        HttpRequestBase httpRequest = new HttpPost(postURL);
        if (params!=null&&params.size()>0) {
            try {
                HttpEntity httpentity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                ((HttpPost) httpRequest).setEntity(httpentity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return httpRequest;
    }
}
