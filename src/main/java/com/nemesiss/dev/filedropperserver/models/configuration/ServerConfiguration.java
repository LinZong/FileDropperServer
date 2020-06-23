package com.nemesiss.dev.filedropperserver.models.configuration;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nemesiss.dev.filedropperserver.FileDropperServerApplication;
import com.nemesiss.dev.filedropperserver.annotations.PropertiesField;
import com.nemesiss.dev.filedropperserver.models.JsonSerializableObject;
import com.nemesiss.dev.filedropperserver.services.configuration.FileDropperConfigurationManager;
import com.nemesiss.dev.filedropperserver.utils.AppUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public final class ServerConfiguration implements JsonSerializableObject {

    public static final String DROPPER_CONFIGURATION_PROPERTY_KEY = "dropper.configuration";
    public static final String DROPPER_CONFIGURATION_SERVER_PROPERTY_KEY = DROPPER_CONFIGURATION_PROPERTY_KEY + ".server";

    @PropertiesField(value = "server.port")
    private Integer managePort;

    private Integer discoveryPort;
    private Integer discoveryTimePeriod;
    private String downloadRootPath;
    private Boolean confirmBeforeReceivingFiles;
    private String machineName;

    @JSONField(serialize = false)
    private boolean fromInternal = false;

    public static ServerConfiguration getDefaultServerConfiguration() {
        Path defaultDownloadPath = Paths.get(System.getProperty("user.home"), "Downloads");
        return new ServerConfiguration(
                8080,
                39393,
                1000,
                defaultDownloadPath.toFile().getAbsolutePath(),
                false,
                AppUtils.resolveComputerName(),
                true);
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

    public void injectPropertiesToSpringEnvironment(ConfigurableApplicationContext cac, SpringApplication springApplication) {
        ConfigurableEnvironment ce = cac.getEnvironment();

        MutablePropertySources propertySources = ce.getPropertySources();
        injectSettingsWithNamespacePrefix(propertySources);
        injectSystemPropertiesFields(propertySources);

        // 把配置文件和配置文件管理器都注入到Bean容器
        cac.getBeanFactory().registerSingleton(this.getClass().getCanonicalName(), this);
        cac.getBeanFactory().registerSingleton(
                FileDropperConfigurationManager.class.getCanonicalName(),
                new FileDropperConfigurationManager(this));
    }

    private void injectSettingsWithNamespacePrefix(MutablePropertySources propertySources) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> fieldToMap = mapper.convertValue(this, new TypeReference<Map<String, Object>>() {
        });
        fieldToMap = fieldToMap.keySet().stream().collect(Collectors.toMap(k -> DROPPER_CONFIGURATION_PROPERTY_KEY + "." + k, fieldToMap::get));
        propertySources.addLast(new MapPropertySource(DROPPER_CONFIGURATION_PROPERTY_KEY, fieldToMap));
    }

    private void injectSystemPropertiesFields(MutablePropertySources propertySources) {
        List<Field> propertiesFields = Arrays
                .stream(this.getClass().getDeclaredFields())
                .filter(x -> x.isAnnotationPresent(PropertiesField.class))
                .collect(Collectors.toList());

        Map<String, Object> propertyPairs = new LinkedHashMap<>(propertiesFields.size());
        for (Field field : propertiesFields) {
            PropertiesField annotation = field.getAnnotation(PropertiesField.class);
            String systemPropertyKey = annotation.value();
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                propertyPairs.put(systemPropertyKey, value);
            } catch (IllegalAccessException e) {
                // Ignored.
            }
        }
        propertySources.addLast(new MapPropertySource(DROPPER_CONFIGURATION_SERVER_PROPERTY_KEY, propertyPairs));
    }

    @Override
    public String toJsonString() {
        return JSON.toJSONString(this);
    }
}
