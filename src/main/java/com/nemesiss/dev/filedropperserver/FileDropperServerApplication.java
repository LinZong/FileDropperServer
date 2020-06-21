package com.nemesiss.dev.filedropperserver;

import com.nemesiss.dev.filedropperserver.services.configuration.FileDropperConfigurationLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.env.SpringApplicationJsonEnvironmentPostProcessor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.support.PropertiesLoaderUtils;

@SpringBootApplication
public class FileDropperServerApplication {

    public static void main(String[] args) {
        FileDropperConfigurationLoader
                .injectServerConfigurationToSpringProperties(
                        new SpringApplication(
                                FileDropperServerApplication.class
                        ))
                .run(args);
    }
}
