/*
 * Power by www.xiaoi.com
 */
package com.eastrobot.robotdev.utils;

import java.security.MessageDigest;

/**
 * 与jquery.md5.js 生成的密文相同，可以通用
 * @author <a href="mailto:eko.z@outlook.com">eko.zhan</a>
 * @date May 7, 2015 11:27:59 AM
 * @version 1.0
 */
public class MD5Util {

	/**
	 * md5加密
	 * @param s
	 * @return
	 */
	public static String encrypt(String s){
		char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
	}
	
	/**
	 * spring salt
	 * @param s
	 * @param salt
	 * @return
	 */
	public static String encrypt(String s, Object salt){
		String saltStr = (String) salt;
		return encrypt(s + "{" + saltStr + "}");
		
	}
	
}
