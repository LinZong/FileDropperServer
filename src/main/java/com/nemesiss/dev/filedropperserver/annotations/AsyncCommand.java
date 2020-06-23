package com.nemesiss.dev.filedropperserver.annotations;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AsyncCommand {
    int commandIndex();
}
