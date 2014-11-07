package com.org.woody.woodylibrary.task;

import android.content.Context;

import com.org.woody.woodylibrary.http.HttpUtils;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * Created by linfaxin on 2014/7/15 015.
 * Email: linlinfaxin@163.com
 * 通过Http方式异步载入一个Object
 */
public abstract class HttpAsyncTask<T> extends ResultAsyncTask<T>{
    HttpRequestBase request;
    public HttpAsyncTask(Context context){
        super(context);
    }
    public HttpAsyncTask(Context context, String url){
        super(context);
        request=new HttpGet(url);
    }
    public HttpAsyncTask(Context context, HttpRequestBase request){
        super(context);
        this.request=request;
    }
    @Override
    protected T doInBackground(Void... params) {
        try {
            String json=loadData();
            return instanceObject(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    protected String loadData(){
        if(request==null) return null;
        return HttpUtils.executeForString(request);
    }
    protected abstract T instanceObject(String json) throws Exception;
}
