package com.nemesiss.dev.filedropperserver.services.configuration.resolver.path;


import com.nemesiss.dev.filedropperserver.models.configuration.ServerConfiguration;

/**
 * Load JSON configuration from outside.
 * Find priority:
 * 1. System.getProperty(); -Ddropper.configuration="xxx"
 * 2. application.yml defined.
 * 3. Jar package default server-configuration.
 * 4. Hardcode default Server Configuration.
 */
public abstract class ServerConfigurationResolver {


    public abstract ServerConfiguration resolve();
}
