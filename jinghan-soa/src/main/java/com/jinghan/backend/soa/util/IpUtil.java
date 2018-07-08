package com.jinghan.backend.soa.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @author Bruce
 * @date 2017/11/2
 */
public class IpUtil {

    /**
     * 获取非127.0.0.1的本地地址
     * @return
     */
    public static String getLocalHostIp(){
        try{
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()){
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()){
                    // loopback地址即本机地址，IPv4的loopback范围是127.0.0.0 ~ 127.255.255.255
                    InetAddress ip = addresses.nextElement();
                    if (ip != null && ip instanceof Inet4Address && !ip.isLoopbackAddress() && !ip.getHostAddress().contains(":")){
                        return ip.getHostAddress();
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
