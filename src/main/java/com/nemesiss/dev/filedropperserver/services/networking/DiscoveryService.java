package com.nemesiss.dev.filedropperserver.services.networking;

import com.nemesiss.dev.filedropperserver.models.command.CommandReply;
import com.nemesiss.dev.filedropperserver.models.command.CommandRequest;
import com.nemesiss.dev.filedropperserver.models.command.SupportCommand;
import com.nemesiss.dev.filedropperserver.models.configuration.ServerConfiguration;
import com.nemesiss.dev.filedropperserver.models.discoveryservice.MachineInfo;
import com.nemesiss.dev.filedropperserver.services.AbstractHandlerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

@Service
@Slf4j
public class DiscoveryService {

    @Autowired
    ServerConfiguration serverConfiguration;

    @Autowired
    AbstractHandlerRegistry handlerRegistry;

    {
        handlerRegistry.registerHandler(SupportCommand.BEGIN_DISCOVERY, this::handleDiscoveryBegin);
        handlerRegistry.registerHandler(SupportCommand.STOP_DISCOVERY, this::handleDiscoveryStop);
        handlerRegistry.registerHandler(SupportCommand.DISCOVERY_RESULT, this::handleGetDiscoveryResult);
    }

    private final LinkedHashMap<String, MachineInfo> discoveryResult = new LinkedHashMap<>();


    private void beginDiscovery() {

        // Begin broadcast package in discovery port and listening outer discovery package come.
    }

    private void stopDiscovery() {

        // Stop netty broadcasting.
    }

    public CommandReply handleGetDiscoveryResult(CommandRequest command) {
        synchronized (discoveryResult) {
            return null;
        }
    }

    public CommandReply handleDiscoveryBegin(CommandRequest command) {
        beginDiscovery();
        return new CommandReply(command.getCommand(), true, "OK", null);
    }

    public CommandReply handleDiscoveryStop(CommandRequest command) {
        stopDiscovery();
        return new CommandReply(command.getCommand(), true, "OK", null);
    }
}
