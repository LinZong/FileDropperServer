package com.nemesiss.dev.filedropperserver.utils;

import com.nemesiss.dev.filedropperserver.models.discoveryservice.NetworkInterfaceCandidate;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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

        List<NetworkInterfaceCandidate> candidates = networkInterfaces
                .stream()
                .filter(NetworkUtils::filterCandidateNIC)
                .map(x -> new NetworkInterfaceCandidate(x.getDisplayName(), x.getInetAddresses().nextElement(), x))
                .collect(Collectors.toList());


        // 尝试优先匹配真是的网卡

        List<NetworkInterfaceCandidate> sortedCandidates = new ArrayList<>(candidates.size());

        Iterator<NetworkInterfaceCandidate> iterator = candidates.iterator();
        while (iterator.hasNext()) {
            NetworkInterfaceCandidate next = iterator.next();
            if (matchNicProducts(next.getNicName())) {
                sortedCandidates.add(next);
                iterator.remove();
            }
        }
        sortedCandidates.addAll(candidates);
        return sortedCandidates;
    }


    private static boolean matchNicProducts(String NicName) {
        String[] products = {"intel", "realtek", "qualcomm", "atheros"};
        String lowerNicName = NicName.toLowerCase();
        for (String product : products) {
            if (lowerNicName.contains(product)) {
                return true;
            }
        }
        return false;
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
