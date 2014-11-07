package com.org.woody.woodylibrary.http;

/**
 * Created by linfaxin on 2014/7/15 015.
 * Email: linlinfaxin@163.com
 */
public class StringResponse {
    String content;
    int code;

    public StringResponse(String content, int code) {
        this.content = content;
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public int getCode() {
        return code;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
