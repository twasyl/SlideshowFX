/*
 * Copyright 2014 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
