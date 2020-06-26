package com.nemesiss.dev.filedropperserver.services;

import com.nemesiss.dev.filedropperserver.models.command.CommandExecutor;
import com.nemesiss.dev.filedropperserver.models.command.CommandRouter;
import org.springframework.stereotype.Service;

@Service
public class FileDropperCore implements CommandRouter {


    @Override
    public synchronized  <T, R> void registerHandler(Class<T> commandType, CommandExecutor<T, R> commandExecutor) {

    }

    @Override
    public <T> void routeCommand(T command) {

    }
}
