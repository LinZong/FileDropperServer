package com.nemesiss.dev.filedropperserver.models.discoveryservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.InetAddress;
import java.net.NetworkInterface;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetworkInterfaceCandidate {

    String NicName;
    InetAddress NicIpAddr;
    NetworkInterface originalInterface;
}
