package com.nemesiss.dev.filedropperserver.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommandReply<T> {

    int commandType;

    boolean success;

    T replyContent;

    Throwable cause;
}
