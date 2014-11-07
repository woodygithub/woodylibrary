package com.org.woody.woodylibrary.http;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

/**
 * Created by linfaxin on 2014/7/15 015.
 * Email: linlinfaxin@163.com
 * 用http方式获取的cookie时，自动与WebView同步（暂时没有处理过期Cookie）
 */
public class WebviewCookieStore implements org.apache.http.client.CookieStore {
    Context context;
    public WebviewCookieStore(Context context){
        this.context=context;
        CookieSyncManager.createInstance(context);
    }
    @Override
    public synchronized void addCookie(Cookie cookie) {
        addCookieImpl(cookie);
    }
    public synchronized void addCookieImpl(Cookie cookie) {
        try {
			CookieManager cookieManager = CookieManager.getInstance();
			String[] cookieInfo = parseCookie(cookie);
			if (cookieInfo != null)
				cookieManager.setCookie(cookieInfo[0], cookieInfo[1]);
			getPreferences().edit().putString(getUrlFromDomain(cookieInfo[0]), cookieInfo[1]).commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    public synchronized void addCookiesImpl(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cooky : cookies) {
                addCookieImpl(cooky);
            }
        }
    }
	@Override
	public synchronized List<Cookie> getCookies() {
		ArrayList<Cookie> cookies= new ArrayList<Cookie>();
        try {
			CookieManager cookieManager = CookieManager.getInstance();
			for (String url : getPreferences().getAll().keySet()) {
				for (Cookie cookie : parseCookie(url, cookieManager.getCookie(url))) {
					cookies.remove(cookie);
					cookies.add(cookie);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cookies;
	}
	@Override
	public void clear() {
		CookieManager.getInstance().removeAllCookie();
	}
	@Override
	public boolean clearExpired(Date date) {
		return false;
	}
	
	/**
	 * @return SharedPreferences,全部的数据都是Cookie，domain = cookie的格式
	 */
	private SharedPreferences getPreferences(){
		return context.getSharedPreferences(getClass().getSimpleName(), 0);
	}
	@SuppressLint("DefaultLocale")
	private String getUrlFromDomain(String domain){
		String url=domain;
		if(url.startsWith(".")) url="www"+url;
		url = url.toLowerCase();
		if ( !url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;
		return url;
	}
    /**
     * @param cookie
     * @return domain,head中Cookie字段
     */
    public String[] parseCookie(Cookie cookie){
        StringBuilder sb = new StringBuilder();
        String domain = cookie.getDomain();
        if (domain == null) return null;
        sb.append(cookie.getName()).append("=").append(cookie.getValue()).append("; ");
        sb.append("path=").append(cookie.getPath() == null ? "/" : cookie.getPath()).append("; ");
        sb.append("domain=").append(domain);
        return new String[]{domain,sb.toString()};
    }
	/**
	 * 根据cookieString 返回Cookie的List
	 * @param url
	 * @param cookieString: name1=value1;name2=value2;...
	 * @return
	 */
	public static List<Cookie> parseCookie(String url, String cookieString) {
		ArrayList<Cookie> list=new ArrayList<Cookie>();
        if(url==null||cookieString==null) return list;
        String[] cookieArray=cookieString.split(";");
        for(String aCookie:cookieArray){
        	String[] ss=aCookie.split("=");
        	if(ss.length==2){
        		try {
            		BasicClientCookie cookie=new BasicClientCookie(ss[0].trim(), ss[1].trim());
					cookie.setDomain(Uri.parse(url).getHost());
	        		list.add(cookie);
				} catch (Exception e) {
				}
        	}
        }
        return list;
    }
}