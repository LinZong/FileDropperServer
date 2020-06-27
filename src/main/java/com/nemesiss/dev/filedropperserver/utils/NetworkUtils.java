package com.nemesiss.dev.filedropperserver.utils;

import com.nemesiss.dev.filedropperserver.models.discoveryservice.NetworkInterfaceCandidate;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.nemesiss.dev.filedropperserver.utils.AppUtils.collectEnumeration;

public class NetworkUtils {

    public static List<NetworkInterfaceCandidate> resolveNicCandidates() {
        List<NetworkInterface> networkInterfaces = Collections.emptyList();
        try {
            networkInterfaces = collectEnumeration(NetworkInterface.getNetworkInterfaces());
        } catch (SocketException e) {
            // TODO human readable exception log.
            e.printStackTrace();
        }

        return networkInterfaces
                .stream()
                .filter(NetworkUtils::filterCandidateNIC)
                .map(x -> new NetworkInterfaceCandidate(x.getDisplayName(), x.getInetAddresses().nextElement(), x))
                .collect(Collectors.toList());
    }


    private static boolean filterCandidateNIC(NetworkInterface nic) {
        try {
            return !nic.isLoopback() && nic.isUp() && !nic.isVirtual();
        } catch (SocketException e) {
            // TODO human readable exception log.
            e.printStackTrace();
            return false;
        }
    }
}
