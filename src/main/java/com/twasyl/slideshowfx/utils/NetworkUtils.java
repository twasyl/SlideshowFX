package com.twasyl.slideshowfx.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Logger;

public class NetworkUtils {
    private static final Logger LOGGER = Logger.getLogger(NetworkUtils.class.getName());

    public static String getIP() {
        String ipAddress = null;

        // Determine the IP address of the machine: any 192.xxx.xxx.xxx should be okay
        final Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();

            Enumeration<InetAddress> inetAddresses;
            InetAddress inet;

            while(interfaces.hasMoreElements() && ipAddress == null) {
                inetAddresses = interfaces.nextElement().getInetAddresses();

                while(inetAddresses.hasMoreElements() && ipAddress == null) {
                    inet = inetAddresses.nextElement();

                    if(inet.getHostAddress().startsWith("192.")) ipAddress = inet.getHostAddress();
                }
            }

            if(ipAddress == null) ipAddress = "localhost";
        } catch (SocketException e) {
           LOGGER.finest("Can not find network interfaces");
            ipAddress = "localhost";
        }

        return ipAddress;
    }
}
