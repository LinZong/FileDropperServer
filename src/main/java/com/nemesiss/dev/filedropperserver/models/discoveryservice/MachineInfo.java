package com.nemesiss.dev.filedropperserver.models.discoveryservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.Inet4Address;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MachineInfo {

    private String machineName;
    private Inet4Address machineIp;
    private Integer discoveryPort;
    private Integer transferTcpPort;

}
