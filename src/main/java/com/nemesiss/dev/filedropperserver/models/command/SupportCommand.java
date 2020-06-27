package com.nemesiss.dev.filedropperserver.models.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupportCommand {


    // File Task Manager Support Commands
    public static final SupportCommand REQUEST_SEND = new SupportCommand("REQUEST_SEND");
    public static final SupportCommand REQUEST_SETTING = new SupportCommand("REQUEST_SETTING");
    public static final SupportCommand REQUEST_PAUSE = new SupportCommand("REQUEST_PAUSE");
    public static final SupportCommand REQUEST_RESUME = new SupportCommand("REQUEST_RESUME");
    public static final SupportCommand REQUEST_REMOVE_TRANSFER_TASK = new SupportCommand("REQUEST_REMOVE_TRANSFER_TASK");
    public static final SupportCommand REQUEST_DELETE_TRANSFER_TASK = new SupportCommand("REQUEST_DELETE_TRANSFER_TASK");
    public static final SupportCommand REQUEST_ALL_TASK_PROPERTIES = new SupportCommand("REQUEST_ALL_TASK_PROPERTIES");

    // Downloader Support Commands
    public static final SupportCommand PAUSE = new SupportCommand("PAUSE");
    public static final SupportCommand FLUSH_META_IMMEDIATELY = new SupportCommand("FLUSH_META_IMMEDIATELY");
    public static final SupportCommand FLUSH_DATA_IMMEDIATELY = new SupportCommand("FLUSH_DATA_IMMEDIATELY");
    public static final SupportCommand RELOAD_PROPERTIES = new SupportCommand("RELOAD_PROPERTIES");

    // Discovery Service Support Commands
    public static final SupportCommand BEGIN_DISCOVERY = new SupportCommand("BEGIN_DISCOVERY");
    public static final SupportCommand STOP_DISCOVERY = new SupportCommand("STOP_DISCOVERY");
    public static final SupportCommand DISCOVERY_RESULT = new SupportCommand("DISCOVERY_RESULT");
    public static final SupportCommand SELECT_DISCOVERY_CANDIDATES = new SupportCommand("SELECT_DISCOVERY_CANDIDATES");
    public static final SupportCommand LIST_DISCOVERY_CANDIDATES = new SupportCommand("LIST_DISCOVERY_CANDIDATES");
    private String name;
}
