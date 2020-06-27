package com.nemesiss.dev.filedropperserver.models.discoveryservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryStatus {

    boolean discovering;

    MachineInfo[] machines;
}
