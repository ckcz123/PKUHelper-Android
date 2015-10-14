package com.pkuhelper.lib.webconnection;

import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

import java.util.HashMap;
import java.util.Map;

public class Cookies {

    private static final HashMap<String, HashMap<String, String>> cookies = new HashMap<String, HashMap<String, String>>();

    private static String getDomain(String url) {
        try {
            String domain = new String(url);
            int pos1 = domain.indexOf("://") + 3;
            domain = url.substring(pos1);
            int pos2 = domain.indexOf("/");
            if (pos2 == -1) return domain;
            return domain.substring(0, pos2).trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取response中的cookies，与url所在域名以Key-Value形式暂存在本地
     * @param response cookies的源
     * @param url cookies将被暂存在*url*所在的域名的Key下
     */
    public static void setCookie(HttpResponse response, String url) {
        String domain = getDomain(url);
        if (domain.equals("")) return;
        HashMap<String, String> hashMap = cookies.get(domain);
        if (hashMap == null) hashMap = new HashMap<String, String>();
        if (!whetherToSetCookie(url)) return;
        Header[] headers = response.getHeaders("Set-cookie");
        if (headers == null || headers.length == 0) return;
        for (int i = 0; i < headers.length; i++) {
            String cookie = headers[i].getValue();
            String[] strings = cookie.split(";");
            String[] strings2 = strings[0].split("=");
            String key = strings2[0].trim();
            String value = strings2.length > 1 ? strings2[1].trim() : "";
            if (!"".equals(key)) {
                Log.w("Set-cookie:", domain + ": " + key + "=" + value);
                //cookies.put(key, value);
                hashMap.put(key, value);
            }
        }
        cookies.put(domain, hashMap);
    }

    /**
     * 将本地暂存的cookies加入Http请求的header标头
     * @param httpRequestBase 将被加入cookies的Http请求
     */
    public static void addCookie(HttpRequestBase httpRequestBase) {
        String domain = getDomain(httpRequestBase.getURI().toString());
        Log.w("domain", domain);
        if (domain.equals("")) return;
        if (!whetherToSendCookie(httpRequestBase)) return;
        HashMap<String, String> hashMap = cookies.get(domain);
        if (hashMap == null) return;
        String cookieString = "";
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            cookieString = cookieString + key + "=" + value + ";";
        }
        Log.w("cookie", cookieString);
        httpRequestBase.addHeader("cookie", cookieString);
    }

    private static boolean whetherToSendCookie(HttpRequestBase httpRequestBase) {
        String url = httpRequestBase.getURI().toString();
        if (url.startsWith("http://course.pku.edu.cn/webapps/login/"))
            return false;
        if (url.startsWith("http://www.bdwm.net/"))
            return false;
        return true;
    }

    private static boolean whetherToSetCookie(String url) {
        if (url.startsWith("http://www.bdwm.net/"))
            return false;
        return true;
    }

}
