package com.eastrobot.robotdev.common;

import com.eastrobot.robotdev.utils.AESUtil;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * 处理app.properties加密的内容
 *
 * @Author alan.peng
 * @Date 2017-09-11 15:34
 */
public class PropertyPlaceholderConfigurerHolder extends PropertyPlaceholderConfigurer {

    @Override
    protected void convertProperties(Properties props) {
        //System.setProperty("propertiesEncrypted","true");
        super.convertProperties(props);
        if("true".equals(System.getProperty("propertiesEncrypted"))) {
            try {
                Iterator var3 = props.entrySet().iterator();
                while(true) {
                    Map.Entry ent;
                    String key;
                    do {
                        do {
                            if(!var3.hasNext()) {
                                return;
                            }

                            ent = (Map.Entry)var3.next();
                            key = (String)ent.getKey();
                        } while(!key.contains("jdbc"));
                    } while(!key.contains("username") && !key.contains("password"));// && !key.contains("url")

                    String val = (String)ent.getValue();
                    props.put(key, AESUtil.decrypt(val));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
