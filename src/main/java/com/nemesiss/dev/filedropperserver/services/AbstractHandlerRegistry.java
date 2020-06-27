package com.nemesiss.dev.filedropperserver.services;

import com.nemesiss.dev.filedropperserver.models.command.*;
import com.nemesiss.dev.filedropperserver.models.exceptions.NoSuchCommandExecutorException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AbstractHandlerRegistry implements CommandRouter {

    @Getter
    private LinkedHashMap<SupportCommand, CommandExecutor> COMMAND_EXECUTOR_MAPS = new LinkedHashMap<>();

    private LinkedBlockingQueue<Runnable> TASK_QUEUE = new LinkedBlockingQueue<>();

    private ThreadPoolExecutor COMMAND_HANDLER_THREAD_POOL = new ThreadPoolExecutor(5, 5, 60, TimeUnit.SECONDS, TASK_QUEUE);


    @Override
    public  void registerHandler(SupportCommand commandType, CommandExecutor commandExecutor) {
        COMMAND_EXECUTOR_MAPS.put(commandType, commandExecutor);
    }

    @SuppressWarnings({"rawtypes"})
    @Override
    public CommandReply routeCommand(CommandRequest command) {

        CommandExecutor executor = COMMAND_EXECUTOR_MAPS.get(command.getCommand());
        if (executor == null) {
            return new CommandReply(
                    command.getCommand(),
                    false,
                    null,
                    new NoSuchCommandExecutorException(command.getCommand()));
        }

        //Validate Parameterized Type

        try {
            return COMMAND_HANDLER_THREAD_POOL.submit(() -> (executor).handleCommand(command)).get();
        } catch (Exception e) {
            log.error("Exception occurred when routing command: {}, reason:{}.", command.getCommand().getName(), e.getMessage());
            return new CommandReply(
                    command.getCommand(),
                    false,
                    null,
                    e
            );
        }
    }

    @Override
    public void gracefullyShutdown() {
        COMMAND_HANDLER_THREAD_POOL.shutdown();
    }
}