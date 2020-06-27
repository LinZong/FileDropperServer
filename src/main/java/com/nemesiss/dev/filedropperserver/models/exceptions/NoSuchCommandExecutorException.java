package com.nemesiss.dev.filedropperserver.models.exceptions;

import com.nemesiss.dev.filedropperserver.models.command.SupportCommand;

public class NoSuchCommandExecutorException extends Exception {

    public NoSuchCommandExecutorException(SupportCommand command) {
        super(String.format("There is no any command executor for such command: %s.", command.getName()));
    }
}
