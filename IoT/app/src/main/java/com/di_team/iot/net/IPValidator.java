package com.di_team.iot.net;

import java.net.InetAddress;

/**Class for validating IP addresses. Validates according to IPv4 prototype*/
public class IPValidator {
    public static boolean isValidIP(String ip) {
        try {
            InetAddress inet = InetAddress.getByName(ip);
            return inet.getHostAddress().equals(ip) && inet instanceof java.net.Inet4Address;
        } catch (Exception e) {
            return false;
        }
    }
}
