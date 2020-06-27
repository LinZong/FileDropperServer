package com.nemesiss.dev.filedropperserver.services.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nemesiss.dev.filedropperserver.models.configuration.ServerConfiguration;
import com.nemesiss.dev.filedropperserver.services.configuration.resolver.path.ServerConfigurationResolver;
import com.nemesiss.dev.filedropperserver.utils.AppUtils;
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

    public static String DEFAULT_CONFIGURATION_NAME = "server-configuration.json";

    public static ServerConfiguration loadServerConfigurationFromFile(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ServerConfiguration serverConfiguration = mapper.readValue(file, ServerConfiguration.class);
        serverConfiguration.mergeDefaultValue(ServerConfiguration.getDefaultServerConfiguration());
        return serverConfiguration;
    }

    static class PackageResourceConfigurationResolver extends ServerConfigurationResolver {

        @Override
        public ServerConfiguration resolve() {
            ClassPathResource cpr = new ClassPathResource(DEFAULT_CONFIGURATION_NAME);
            if (cpr.exists()) {
                try {
                    byte[] result = IOUtils.toByteArray(cpr.getInputStream());
                    String resultJson = new String(result, StandardCharsets.UTF_8);
                    ObjectMapper mapper = new ObjectMapper();
                    ServerConfiguration serverConfiguration = mapper.readValue(resultJson, ServerConfiguration.class);
                    serverConfiguration.mergeDefaultValue(ServerConfiguration.getDefaultServerConfiguration());
                    extractConfigurationToWorkingDirectory(serverConfiguration);
                    return serverConfiguration;
                } catch (Exception e) {
                    log.error("PackageResourceConfigurationResolver load ServerConfiguration from: classpath:server-configuration.json failed! Reason: {}",
                            e.getMessage());
                }
            }
            return null;
        }

        private static void extractConfigurationToWorkingDirectory(ServerConfiguration serverConfiguration) {
            log.warn("Extracting server-configuration.json to your working directory: {}", AppUtils.CWD);
            log.warn("It's strongly recommended that customize this server using external configuration file at the same work directory with the current running program.");
            try {
                FileDropperConfigurationManager.writeConfigurationToPathAsJsonString(serverConfiguration, AppUtils.CWD);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalStateException | IllegalArgumentException ise) {
                log.error(ise.getMessage());
            }
        }
    }

    static class SpringApplicationPropertiesConfigurationResolver extends ServerConfigurationResolver {
        @Override
        public ServerConfiguration resolve() {
            File file = new File(DEFAULT_CONFIGURATION_NAME);
            if (file.exists() && file.canRead()) {
                try {
                    return loadServerConfigurationFromFile(file);
                } catch (NullPointerException npe) {
                    // Ignored.
                } catch (Exception e) {
                    log.error("SpringApplicationPropertiesConfigurationResolver load ServerConfiguration from: {} failed! Reason: {}",
                            DEFAULT_CONFIGURATION_NAME,
                            e.getMessage());
                }
            }
            return null;
        }
    }

    static class SystemPropertyParameterResolver extends ServerConfigurationResolver {
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

        private final SpringApplication loadingApplication;

        public SpringApplicationPropertiesLoadedListener(SpringApplication loadingApplication) {
            this.loadingApplication = loadingApplication;
        }

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
                    DEFAULT_CONFIGURATION_NAME = configurationFilePath;
                }
                new FileDropperConfigurationLoader().beginResolve(applicationPreparedEvent.getApplicationContext(), loadingApplication);
            }
        }
    }

    private static final ServerConfigurationResolver[] resolvers = new ServerConfigurationResolver[]
            {
                    new SystemPropertyParameterResolver(),
                    new SpringApplicationPropertiesConfigurationResolver(),
                    new PackageResourceConfigurationResolver()
            };


    public void beginResolve(ConfigurableApplicationContext configurableApplicationContext, SpringApplication springApplication) {
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
        log.info("Loaded Server Configuration is: {}", serverConfiguration.toString());
        serverConfiguration.injectPropertiesToSpringEnvironment(configurableApplicationContext, springApplication);
    }

    public static SpringApplication injectServerConfigurationToSpringProperties(SpringApplication springApplication) {
        springApplication.addListeners(new SpringApplicationPropertiesLoadedListener(springApplication));
        return springApplication;
    }
}
