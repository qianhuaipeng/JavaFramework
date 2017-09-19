package com.eastrobot.robotdev.utils;

import com.eastrobot.robotdev.common.MethodDefinition;
import com.eastrobot.robotdev.common.ProxyInvoker;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * 加解密工具类
 *
 * @Author alan.peng
 * @Date 2017-09-11 15:33
 */
public class AESUtil {

    public static final String T = ie() + ProxyInvoker.h() + MethodDefinition.d();
    private static IvParameterSpec IV;

    static {
        byte[] ivBytes = new byte[16];

        for(int i = 1; i <= 16; ++i) {
            ivBytes[i - 1] = (byte)(i & 255);
        }

        IV = new IvParameterSpec(ivBytes);
    }

    public AESUtil() {
    }

    public static Key generateKey() {
        return new SecretKeySpec(T.substring(0, 16).getBytes(), "AES");
    }

    public static String encrypt(String input) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(1, generateKey(), IV);
            byte[] result = cipher.doFinal(input.getBytes("UTF-8"));
            return byte2HexStr(result);
        } catch (Exception var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public static String decrypt(String input) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(2, generateKey(), IV);
            byte[] result = cipher.doFinal(str2ByteArray(input));
            return new String(result, "UTF-8");
        } catch (Exception var3) {
            var3.printStackTrace();
            return null;
        }
    }

    private static String byte2HexStr(byte[] b) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < b.length; ++i) {
            String s = Integer.toHexString(b[i] & 255);
            if(s.length() == 1) {
                sb.append("0");
            }

            sb.append(s.toUpperCase());
        }

        return sb.toString();
    }

    private static byte[] str2ByteArray(String s) {
        int byteArrayLength = s.length() / 2;
        byte[] b = new byte[byteArrayLength];

        for(int i = 0; i < byteArrayLength; ++i) {
            byte b0 = (byte)Integer.valueOf(s.substring(i * 2, i * 2 + 2), 16).intValue();
            b[i] = b0;
        }

        return b;
    }

    public static String ie() {
        return String.valueOf(2147483647).substring(0, 6);
    }

    public static String d() {
        String s = String.valueOf(3.141592653589793D).substring(2);
        StringBuilder buf = new StringBuilder();

        for(int i = s.length() - 1; i >= 0; --i) {
            buf.append(s.charAt(i));
        }

        return s + String.valueOf(3.141592653589793D).substring(2, 9) + buf.toString();
    }

    public static String h() {
        return AESUtil.class.getSimpleName().toLowerCase().substring(2, 7);
    }

    public static void main(String[] args) {
        String encrypt = encrypt("alan");
        System.out.println(encrypt);
        //System.out.println(decrypt("F2F7E0EA2AFEAFED6584B46F0D63C093"));
    }

}
