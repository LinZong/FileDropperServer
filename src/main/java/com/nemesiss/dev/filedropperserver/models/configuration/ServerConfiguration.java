package com.nemesiss.dev.filedropperserver.models.configuration;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ServerConfiguration {

    public static final String DROPPER_CONFIGURATION_PROPERTY_KEY = "dropper.configuration";

    private Integer managePort;
    private Integer discoveryPort;
    private Integer discoveryTimePeriod;
    private String downloadRootPath;
    private Boolean confirmBeforeReceivingFiles;

    public static ServerConfiguration getDefaultServerConfiguration() {
        Path defaultDownloadPath = Paths.get(System.getProperty("user.home"), "Downloads");
        return new ServerConfiguration(8080, 39393, 1000, defaultDownloadPath.toFile().getAbsolutePath(), false);
    }

    public void mergeDefaultValue(ServerConfiguration defaultValueObj) {
        for (Field declaredField : ServerConfiguration.class.getDeclaredFields()) {
            declaredField.setAccessible(true);
            try {
                if (declaredField.get(this) == null) {
                    declaredField.set(this, declaredField.get(defaultValueObj));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                log.error("Merge field: {} failed.", declaredField.getName());
            }
        }
    }

    public void injectPropertiesToSpringEnvironment(ConfigurableApplicationContext cac) {
        ConfigurableEnvironment ce = cac.getEnvironment();

        MutablePropertySources propertySources = ce.getPropertySources();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> fieldToMap = mapper.convertValue(this, new TypeReference<Map<String, Object>>() {});

        fieldToMap = fieldToMap.keySet().stream().collect(Collectors.toMap(k -> DROPPER_CONFIGURATION_PROPERTY_KEY + "." + k, fieldToMap::get));
        propertySources.addLast(new MapPropertySource(DROPPER_CONFIGURATION_PROPERTY_KEY, fieldToMap));
        cac.getBeanFactory().registerSingleton(this.getClass().getCanonicalName(), this);
    }
}
