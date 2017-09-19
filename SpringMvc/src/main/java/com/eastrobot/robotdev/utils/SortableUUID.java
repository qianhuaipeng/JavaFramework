package com.eastrobot.robotdev.utils;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 * @Author: alan.peng
 * @Description:
 * @Date: Create in 15:02 2017/8/17
 * @Modified By：
 */
public class SortableUUID {

    private static String mac;
    private static int seq;

    static {
        try {
            mac = (String)getMac().get(0);
            seq = 0;
        } catch (Exception var1) {
            throw new IllegalStateException(var1.getCause());
        }
    }

    public SortableUUID() {
    }

    public static String randomUUID(Date date) {
        return getTimeMillis(date) + getSeq() + mac;
    }

    public static String randomUUID() {
        return getTimeMillis((Date)null) + getSeq() + mac;
    }

    private static synchronized String getSeq() {
        if(seq >= 99999) {
            seq = 0;
        }

        String sseq = String.valueOf(seq++);
        int length = 5 - sseq.length();

        for(int i = 0; i < length; ++i) {
            sseq = "0" + sseq;
        }

        return sseq;
    }

    private static String getTimeMillis(Date date) {
        String millis = null;
        if(date != null) {
            millis = String.valueOf(date.getTime());
        } else {
            millis = String.valueOf(System.currentTimeMillis());
        }

        int length = 15 - millis.length();

        for(int i = 0; i < length; ++i) {
            millis = "0" + millis;
        }

        return millis;
    }

    private static List<String> getMac() throws SocketException {
        ArrayList<String> macList = new ArrayList();
        Enumeration nis = NetworkInterface.getNetworkInterfaces();

        while(true) {
            byte[] mac;
            do {
                if(!nis.hasMoreElements()) {
                    return macList;
                }

                NetworkInterface ni = (NetworkInterface)nis.nextElement();
                mac = ni.getHardwareAddress();
            } while(mac == null);

            StringBuilder builder = new StringBuilder();
            byte[] var8 = mac;
            int var7 = mac.length;

            for(int var6 = 0; var6 < var7; ++var6) {
                byte b = var8[var6];
                builder.append(toHexString(b));
            }

            macList.add(builder.toString());
        }
    }

    private static String toHexString(byte b) {
        String ret = Integer.toHexString(255 & b).toLowerCase();
        if(ret.length() == 1) {
            ret = "0" + ret;
        }

        return ret;
    }

    public static void main(String[] args) throws Exception {
        for(int i = 0; i < 100; ++i) {
            System.out.println(randomUUID());
        }

    }
}
