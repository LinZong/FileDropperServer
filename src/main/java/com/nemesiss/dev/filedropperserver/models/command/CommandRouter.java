package com.nemesiss.dev.filedropperserver.models.command;

import java.util.concurrent.Future;

public interface CommandRouter {

    void registerHandler(SupportCommand commandType, CommandExecutor commandExecutor);

    CommandReply routeCommand(CommandRequest command);

    void gracefullyShutdown();
}
