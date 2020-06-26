package com.nemesiss.dev.filedropperserver.models.command;

import java.util.concurrent.Future;

public interface CommandExecutor<T, R> {

    CommandReply<R> handleCommand(T command);

    Future<CommandReply<R>> handleCommandAsync(T command);
}
