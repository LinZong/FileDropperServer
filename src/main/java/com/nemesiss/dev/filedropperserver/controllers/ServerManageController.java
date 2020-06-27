package com.nemesiss.dev.filedropperserver.controllers;

import com.nemesiss.dev.filedropperserver.models.command.CommandReply;
import com.nemesiss.dev.filedropperserver.models.command.CommandRequest;
import com.nemesiss.dev.filedropperserver.models.command.SupportCommand;
import com.nemesiss.dev.filedropperserver.models.configuration.ServerConfiguration;
import com.nemesiss.dev.filedropperserver.models.discoveryservice.MachineInfo;
import com.nemesiss.dev.filedropperserver.services.FileDropperCore;
import com.nemesiss.dev.filedropperserver.services.networking.DiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/server/")
public class ServerManageController {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    ServerConfiguration serverConfiguration;

    @Autowired
    FileDropperCore fileDropperCore;

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
    public CommandReply getDiscoveredEndpoint() {
        return fileDropperCore.routeCommand(new CommandRequest(SupportCommand.DISCOVERY_RESULT, null));
    }

    @PostMapping("discovery/switch/{status}")
    public CommandReply changeDiscoverySwitch(@PathVariable(name = "status") boolean status) {
        if (status) {
            return fileDropperCore.routeCommand(new CommandRequest(SupportCommand.BEGIN_DISCOVERY, null));
        }
        return fileDropperCore.routeCommand(new CommandRequest(SupportCommand.STOP_DISCOVERY, null));
    }
}
