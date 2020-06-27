package com.nemesiss.dev.filedropperserver.models.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandRequest {

    SupportCommand command;

    Object commandContent;
}
