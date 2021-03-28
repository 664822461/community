package com.nowcoder.community.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {


    //生成随机的字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replace("-","");
    }

    //MD5加密
    public static String md5(String key,String salt){
        if(StringUtils.isBlank(key)){
            return null;
        }
        String s = DigestUtils.md5DigestAsHex(key.getBytes());
        return DigestUtils.md5DigestAsHex((s+salt).getBytes());
    }

}
