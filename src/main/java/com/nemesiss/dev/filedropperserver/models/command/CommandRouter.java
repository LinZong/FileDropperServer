package com.nemesiss.dev.filedropperserver.models.command;

public interface CommandRouter {

    <T,R>  void registerHandler(Class<T> commandType, CommandExecutor<T,R> commandExecutor);

    <T> void routeCommand(T command);
}
