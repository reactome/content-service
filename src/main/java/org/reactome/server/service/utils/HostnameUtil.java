package org.reactome.server.service.utils;

import java.net.InetAddress;

public final class HostnameUtil {

    private HostnameUtil() {}

    public static boolean matchesHostname(String hostname) {
        try {
            return hostname.equalsIgnoreCase(InetAddress.getLocalHost().getHostName());
        } catch (Throwable t) {
            return false;
        }
    }
}
