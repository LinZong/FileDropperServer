package com.nemesiss.dev.filedropperserver.models.command;

public class SupportCommand {

    public enum TransferTaskManager {
        REQUEST_SEND(false),
        REQUEST_SETTING(false),
        REQUEST_PAUSE(false),
        REQUEST_RESUME(false),
        REQUEST_REMOVE_TRANSFER_TASK(false),
        REQUEST_DELETE_TRANSFER_TASK(false),
        REQUEST_ALL_TASK_PROPERTIES(false);

        boolean async;

        TransferTaskManager(boolean async) {
            this.async = async;
        }
    }

    public enum Downloader {
        PAUSE(true),
        FLUSH_META_IMMEDIATELY(true),
        FLUSH_DATA_IMMEDIATELY(true),
        RELOAD_PROPERTIES(true);

        boolean async;

        Downloader(boolean async) {
            this.async = async;
        }
    }

    public enum DiscoveryService {
        BEGIN_DISCOVERY(false),
        STOP_DISCOVERY(false),
        RELOAD_PROPERTIES(false),
        DISCOVERY_RESULT(false);

        boolean async;

        DiscoveryService(boolean async) {
            this.async = async;
        }
    }
}
