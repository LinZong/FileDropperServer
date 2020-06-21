package com.nemesiss.dev.filedropperserver.services.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nemesiss.dev.filedropperserver.models.configuration.ServerConfiguration;
import com.nemesiss.dev.filedropperserver.services.configuration.resolver.path.ServerConfigurationResolver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.nemesiss.dev.filedropperserver.models.configuration.ServerConfiguration.DROPPER_CONFIGURATION_PROPERTY_KEY;


@Slf4j
public class FileDropperConfigurationLoader {

    private static String applicationPropertiesDefinedServerConfigurationFilePath = "server-configuration.json";

    public static ServerConfiguration loadServerConfigurationFromFile(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ServerConfiguration serverConfiguration = mapper.readValue(file, ServerConfiguration.class);
        serverConfiguration.mergeDefaultValue(ServerConfiguration.getDefaultServerConfiguration());
        return serverConfiguration;
    }

    static class PackageResourceConfigurationResolver implements ServerConfigurationResolver {

        @Override
        public ServerConfiguration resolve() {
            ClassPathResource cpr = new ClassPathResource(applicationPropertiesDefinedServerConfigurationFilePath);
            if (cpr.exists()) {
                try {
                    byte[] result = IOUtils.toByteArray(cpr.getInputStream());
                    String resultJson = new String(result, StandardCharsets.UTF_8);
                    ObjectMapper mapper = new ObjectMapper();
                    ServerConfiguration serverConfiguration = mapper.readValue(resultJson, ServerConfiguration.class);
                    serverConfiguration.mergeDefaultValue(ServerConfiguration.getDefaultServerConfiguration());
                    return serverConfiguration;
                } catch (Exception e) {
                    log.error("PackageResourceConfigurationResolver load ServerConfiguration from: classpath:server-configuration.json failed! Reason: {}",
                            e.getMessage());
                }
            }
            return null;
        }
    }

    static class SpringApplicationPropertiesConfigurationResolver implements ServerConfigurationResolver {
        @Override
        public ServerConfiguration resolve() {
            File file = new File(applicationPropertiesDefinedServerConfigurationFilePath);
            if (file.exists() && file.canRead()) {
                try {
                    return loadServerConfigurationFromFile(file);
                } catch (NullPointerException npe) {
                    // Ignored.
                } catch (Exception e) {
                    log.error("SpringApplicationPropertiesConfigurationResolver load ServerConfiguration from: {} failed! Reason: {}",
                            applicationPropertiesDefinedServerConfigurationFilePath,
                            e.getMessage());
                }
            }
            return null;
        }
    }

    static class SystemPropertyParameterResolver implements ServerConfigurationResolver {
        @Override
        public ServerConfiguration resolve() {
            try {
                String configureFilePath = System.getProperty(DROPPER_CONFIGURATION_PROPERTY_KEY);
                File file = new File(configureFilePath);
                if (file.exists() && file.canRead()) {
                    return loadServerConfigurationFromFile(file);
                }
            } catch (NullPointerException npe) {
                // Ignored.
            } catch (Exception e) {
                log.error("SystemPropertyParameterResolver load ServerConfiguration from system property: {} failed! Reason: {}",
                        DROPPER_CONFIGURATION_PROPERTY_KEY,
                        e.getMessage());
            }
            return null;
        }
    }

    static class SpringApplicationPropertiesLoadedListener implements GenericApplicationListener {
        @Override
        public boolean supportsEventType(ResolvableType eventType) {
            return eventType.equals(ResolvableType.forClass(ApplicationPreparedEvent.class));
        }

        @Override
        public void onApplicationEvent(ApplicationEvent event) {
            if (event instanceof ApplicationPreparedEvent) {
                ApplicationPreparedEvent applicationPreparedEvent = (ApplicationPreparedEvent) event;
                String configurationFilePath = applicationPreparedEvent.getApplicationContext().getEnvironment().getProperty(DROPPER_CONFIGURATION_PROPERTY_KEY);
                if (StringUtils.hasText(configurationFilePath)) {
                    applicationPropertiesDefinedServerConfigurationFilePath = configurationFilePath;
                    new FileDropperConfigurationLoader().beginResolve(applicationPreparedEvent.getApplicationContext());
                }
            }
        }
    }

    private static final ServerConfigurationResolver[] resolvers = new ServerConfigurationResolver[]
            {
                    new SystemPropertyParameterResolver(),
                    new SpringApplicationPropertiesConfigurationResolver(),
                    new PackageResourceConfigurationResolver()
            };


    public void beginResolve(ConfigurableApplicationContext configurableApplicationContext) {
        ServerConfiguration serverConfiguration = null;

        for (ServerConfigurationResolver resolver : resolvers) {
            serverConfiguration = resolver.resolve();
            if (serverConfiguration != null) {
                log.info("Server configuration injected using resolver: {}", resolver.getClass().getSimpleName());
                break;
            }
        }

        if (serverConfiguration == null) {
            serverConfiguration = ServerConfiguration.getDefaultServerConfiguration();
        }
        serverConfiguration.injectPropertiesToSpringEnvironment(configurableApplicationContext);
    }

    public static SpringApplication injectServerConfigurationToSpringProperties(SpringApplication springApplication) {
        springApplication.addListeners(new SpringApplicationPropertiesLoadedListener());
        return springApplication;
    }
}
