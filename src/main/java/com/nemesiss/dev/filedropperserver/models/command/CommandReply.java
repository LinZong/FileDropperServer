package com.nemesiss.dev.filedropperserver.models.command;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommandReply {

    SupportCommand command;

    boolean success;

    Object replyContent;

    Throwable cause;

    public static final String SUCCESS_REPLY = "OK";
}
