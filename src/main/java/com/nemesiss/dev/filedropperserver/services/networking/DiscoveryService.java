package com.nemesiss.dev.filedropperserver.services.networking;

import com.nemesiss.dev.filedropperserver.models.command.CommandExecutor;
import com.nemesiss.dev.filedropperserver.models.command.CommandReply;
import com.nemesiss.dev.filedropperserver.models.command.SupportCommand;
import com.nemesiss.dev.filedropperserver.models.discoveryservice.MachineInfo;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

@Service
public class DiscoveryService implements CommandExecutor<SupportCommand.DiscoveryService, CommandReply<MachineInfo>> {


    @Override
    public CommandReply<CommandReply<MachineInfo>> handleCommand(SupportCommand.DiscoveryService command) {
        return null;
    }

    @Override
    public Future<CommandReply<CommandReply<MachineInfo>>> handleCommandAsync(SupportCommand.DiscoveryService command) {
        return null;
    }

}
