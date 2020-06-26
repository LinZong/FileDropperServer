package com.nemesiss.dev.filedropperserver.models.command;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommandReply<T> {

    @JSONField(serialize = false)
    Class<?> commandType;

    boolean success;

    T replyContent;

    Throwable cause;
}
