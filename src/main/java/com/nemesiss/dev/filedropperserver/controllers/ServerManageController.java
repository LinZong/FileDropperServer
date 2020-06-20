package com.nemesiss.dev.filedropperserver.controllers;

import com.nemesiss.dev.filedropperserver.models.properties.ServerDiscoveryProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

@RestController
@RequestMapping("/server/")
public class ServerManageController {


    @Autowired
    ServerDiscoveryProperties discoveryProperties;

    @GetMapping("health")
    public String healthyCheck() {
        return "OK  " + discoveryProperties.toString();
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
