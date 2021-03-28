package com.nowcoder.community.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtils {

    public static String getValue(HttpServletRequest httpServletRequest,String key){
        if (httpServletRequest == null || key == null) {
            throw new IllegalArgumentException("参数为空!");
        }

        Cookie[] cookies = httpServletRequest.getCookies();

        for(Cookie cookie:cookies){
            if(cookie.getName().equals(key)){
               return cookie.getValue();
            }
        }
        return null;
    }
}
