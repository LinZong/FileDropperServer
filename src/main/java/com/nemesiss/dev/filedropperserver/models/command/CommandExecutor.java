package com.nemesiss.dev.filedropperserver.models.command;

import java.util.concurrent.Future;

public interface CommandExecutor {

    CommandReply handleCommand(CommandRequest command);

}
