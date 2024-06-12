package com.winter.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 系统工具类，获取主机信息
 * */
public class SystemUtil {

    /**
     * 获取主机名
     * */
    public static String getHostname(){
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return hostname;
    }
}
