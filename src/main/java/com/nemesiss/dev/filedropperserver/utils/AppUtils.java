package com.nemesiss.dev.filedropperserver.utils;

import com.nemesiss.dev.filedropperserver.models.discoveryservice.NetworkInterfaceCandidate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

@Slf4j
public class AppUtils {

    public static final String CWD = System.getProperty("user.dir");

    public static String resolveComputerName() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return System.getenv("COMPUTERNAME");
        }
        if (SystemUtils.IS_OS_LINUX) { // TODO Add macOS support here.
            Runtime shell = Runtime.getRuntime();
            try {
                Process p = shell.exec("uname --nodename");
                return IOUtils.toString(p.getInputStream(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("Cannot resolveComputerName on current Linux distribution version. Reason: {}", e.getMessage());
                e.printStackTrace();
            }
        }
        // Fallback to random name.
        return "FileDropperServer-" + new Random().nextInt(Integer.MAX_VALUE);
    }



    public static <T> List<T> collectEnumeration(Enumeration<T> enumeration) {
        List<T> result = new ArrayList<>();
        CollectionUtils.addAll(result, enumeration);
        return result;
    }
}
