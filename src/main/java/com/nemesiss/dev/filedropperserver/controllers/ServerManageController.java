package com.nemesiss.dev.filedropperserver.controllers;

import com.nemesiss.dev.filedropperserver.models.configuration.ServerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/server/")
public class ServerManageController {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    ServerConfiguration serverConfiguration;

    @Value("${dropper.configuration.managePort}")
    int managePort;

    @GetMapping("health")
    public String healthyCheck() {
        return "OK" + serverConfiguration.getDownloadRootPath() + managePort;
    }

    @GetMapping("profile")
    public String getServerProfile() {
        return "Server Profile";
    }

    @PostMapping("profile")
    public String applyServerProfile() {
        return "Applied!";
    }

    @GetMapping("discovery/endpoint")
    public String getDiscoveredEndpoint() {
        return "Nothing yet.";
    }

    @PostMapping("discovery/switch/{status}")
    public String changeDiscoverySwitch(@PathVariable(name = "status") boolean status) {
        return "Status: " + status;
    }
}
