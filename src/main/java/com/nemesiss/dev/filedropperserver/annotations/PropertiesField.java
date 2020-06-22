package com.nemesiss.dev.filedropperserver.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PropertiesField {
    String value();
}
