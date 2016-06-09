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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides utility methods for working with IP address.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
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
     * Get IPv4 addresses for every {@link java.net.NetworkInterface} on the machine. This method never returns {@code null}.
     * If no interfaces are found, an empty list is returned.
     * The list of addresses is sorted lexicographically.
     *
     * @return The list of all IP addresses.
     */
    public static List<String> getIPs() {
        final List<String> ips = new ArrayList<>();

        final String ipAddressRegex = "[1-9][0-9]{0,2}\\.[0-9]{0,3}\\.[0-9]{0,3}\\.[0-9]{0,3}";
        final Pattern ipAddressPattern = Pattern.compile(ipAddressRegex);
        final Enumeration<NetworkInterface> interfaces;
        Matcher ipAddressMatcherMatcher;

        try {
            interfaces = NetworkInterface.getNetworkInterfaces();

            Enumeration<InetAddress> inetAddresses;
            InetAddress inet;

            while(interfaces.hasMoreElements()) {
                inetAddresses = interfaces.nextElement().getInetAddresses();

                while(inetAddresses.hasMoreElements()) {
                    inet = inetAddresses.nextElement();

                    ipAddressMatcherMatcher = ipAddressPattern.matcher(inet.getHostAddress());

                    if(ipAddressMatcherMatcher.matches()) ips.add(inet.getHostAddress());
                }
            }

            Collections.sort(ips);
        } catch (SocketException e) {
            LOGGER.finest("Can not find network interfaces");
        }

        return ips;
    }

    /**
     * Get an {@link javafx.collections.ObservableList} containing the list of IP addresses of the machine.
     * This method calls {@link #getIPs()} to get the addresses.
     * @return An observable list of all IP addresses of the machine.
     */
    public static ObservableList<String> getObservableIps() {
        return FXCollections.observableArrayList(getIPs());
    }
}
