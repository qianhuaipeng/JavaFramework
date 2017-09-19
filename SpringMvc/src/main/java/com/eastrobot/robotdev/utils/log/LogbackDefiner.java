package com.eastrobot.robotdev.utils.log;

import ch.qos.logback.core.PropertyDefinerBase;
import com.eastrobot.robotdev.utils.SystemKeys;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: alan.peng
 * @Description:
 * @Date: Create in 10:05 2017/8/21
 * @Modified By：
 */
public class LogbackDefiner extends PropertyDefinerBase {


    @Override
    public String getPropertyValue() {
        String webRoot = SystemKeys.getDir();
        if (StringUtils.isBlank(webRoot)){
            webRoot = System.getProperty("user.dir");
        }
        return webRoot;
    }


}
