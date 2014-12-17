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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

/**
 * Provides utility methods for working with IP address.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class NetworkUtils {
    private static final Logger LOGGER = Logger.getLogger(NetworkUtils.class.getName());

    /**
     * Get the first IP address starting with {@code 192.}. If no address matching that criterion is found, {@code localhost}
     * is returned.
     * In case of an exception, {@code localhost} is returned.
     *
     * @return The IP address of the machine.
     */
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

    /**
     * Get IP addresses for every {@link java.net.NetworkInterface} on the machine. This method never returns {@code null}.
     * If no interfaces are found, an empty list is returned.
     * The list of addresses is sorted lexicographically.
     *
     * @return The list of all IP addresses.
     */
    public static List<String> getIPs() {
        final List<String> ips = new ArrayList<>();

        final Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();

            Enumeration<InetAddress> inetAddresses;
            InetAddress inet;

            while(interfaces.hasMoreElements()) {
                inetAddresses = interfaces.nextElement().getInetAddresses();

                while(inetAddresses.hasMoreElements()) {
                    inet = inetAddresses.nextElement();

                    ips.add(inet.getHostAddress());
                }
            }

            Collections.sort(ips);
        } catch (SocketException e) {
            LOGGER.finest("Can not find network interfaces");
        }

        return ips;
    }

    /**
     * Get an {@link javafx.collections.ObservableList<String>} containing the list of IP addresses of the machine.
     * This method calls {@link #getIPs()} to get the addresses.
     * @return An observable list of all IP addresses of the machine.
     */
    public static ObservableList<String> getObservableIps() {
        return FXCollections.observableArrayList(getIPs());
    }
}
