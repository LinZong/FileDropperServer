package com.nemesiss.dev.filedropperserver.models.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "dropper.server.discovery", ignoreInvalidFields = true)
@Validated
@Data
public class ServerDiscoveryProperties {


    private int port;

    private Duration timePeriod;

    private boolean enabled = true;

}
