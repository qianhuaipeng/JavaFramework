package com.eastrobot.robotdev.utils.log;

import com.eastrobot.robotdev.utils.SystemKeys;
import com.eastrobot.robotdev.utils.date.DateUtil;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @Author: alan.peng
 * @Description: 日志游标类
 * @Date: Create in 13:53 2017/8/17
 * @Modified By：
 */
public class LogMarker {

    private static PropertiesConfiguration config;

    public static final String LOG_FILE_CALL = "call.log";
    public static final String KEY_CALL = "call";

    static {
        try {
            config = new PropertiesConfiguration("log.properties");
            addPosKey(KEY_CALL);
            addDateKey(KEY_CALL);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

    }

    /**
     * 创建 pos key 并初始化值
     *
     * @author eko.zhan at Sep 12, 2016 2:05:40 PM
     * @param key
     */
    private static void addPosKey(String key) {
        String posKey = getPosKey(key);
        if (StringUtils.isBlank(config.getString(posKey))) {//getPosKey(posKey)
            config.setProperty(posKey, 0);
            try {
                config.save();
            } catch (ConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建 date key 并初始化值
     *
     * @author eko.zhan at Sep 12, 2016 2:06:22 PM
     * @param key
     */
    private static void addDateKey(String key) {
        String dateKey = getDateKey(key);
        if (StringUtils.isBlank(config.getString(dateKey))) {
            config.setProperty(dateKey, DateUtil.formtDate(new Date()));
            try {
                config.save();
            } catch (ConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    public static long getPos(String key) {
        return config.getLong(getPosKey(key));
    }

    public static void setPos(String key, long pos) {
        config.setProperty(getPosKey(key), pos);
    }

    public static String getDate(String key) {
        return config.getString(getDateKey(key));
    }

    public static void setDate(String key, String date) {
        config.setProperty(getDateKey(key), date);
    }

    /**
     * 是否是今天
     *
     * @return
     */
    public static boolean isToday(String key) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        return DateUtil.formtDate(cal.getTime()).equals(LogMarker.getDate(key));
    }

    /**
     * 日志游标日期往前滚1天，并保存
     */
    public static void addDay(String key) {
        if (!isToday(key)) {
            Calendar cursorCal = Calendar.getInstance();
            cursorCal.setTime(DateUtil.date(getDate(key)));
            cursorCal.add(Calendar.DATE, 1);
            LogMarker.setDate(key, DateUtil.formtDate(cursorCal.getTime()));
            LogMarker.setPos(key, 0);
            LogMarker.save();
        }
    }

    /**
     * 获取应该读取的日志文件路径
     *
     * @return
     */
    public static String getLogFilePath(String key, String logFile) {
        //System.getProperty("jetty.home");
        String logPath = SystemKeys.getDir() + File.separator + "WEB-INF" + File.separator + "logs" + File.separator + logFile;
        //String logPath = System.getProperty("user.dir") + File.separator + "WEB-INF" + File.separator + "logs" + File.separator + logFile;
        if (!isToday(key)) {
            logPath += "." + getDate(key);
        }
        return logPath;
    }

    /**
     * 获取指定日期的日志文件
     *
     * @author eko.zhan at Sep 18, 2016 5:37:55 PM
     * @param logFile
     * @param days
     *            负整数
     * @return
     */
    public static String getLogFilePath(String logFile, int days) {
        //String logPath = SystemKeys.getDir() + File.separator + "WEB-INF" + File.separator + "logs" + File.separator + logFile;
        String logPath = System.getProperty("jetty.home") + File.separator + "logs" + File.separator + logFile;
        if (days != 0) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, days);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            logPath += "." + sdf.format(cal.getTime());
        }
        return logPath;
    }

    /**
     * 保存当前游标文件
     */
    public static void save() {
        try {
            config.save();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * 包装 关键词
     *
     * @author eko.zhan at Sep 18, 2016 11:39:42 AM
     * @param key
     * @return
     */
    private static String getDateKey(String key) {
        return "date." + key;
    }

    /**
     * 包装 关键词
     *
     * @author eko.zhan at Sep 18, 2016 11:39:45 AM
     * @param key
     * @return
     */
    private static String getPosKey(String key) {
        return "pos." + key;
    }
}
