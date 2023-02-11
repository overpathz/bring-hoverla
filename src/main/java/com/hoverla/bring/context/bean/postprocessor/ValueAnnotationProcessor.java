package com.hoverla.bring.context.bean.postprocessor;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.annotation.Value;
import com.hoverla.bring.context.ApplicationContext;
import com.hoverla.bring.exception.InitializePropertyException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * ValueAnnotationProcessor using to activate features to inject value from the configuration file
 * to the field.
 *
 * @see Bean
 */
@Slf4j
public class ValueAnnotationProcessor implements PostProcessor {

    private Map<String, String> propertiesMap = new HashMap<>();

    public ValueAnnotationProcessor() {
        initPropertiesMap();
    }

    @Override
    @SuppressWarnings("java:S3011")
    public void process(Object beanInstance, ApplicationContext applicationContext) {
        for (Field field : beanInstance.getClass().getDeclaredFields()) {
            try {
                if (field.isAnnotationPresent(Value.class)) {
                    log.trace("Trying to set value from @Value annotation for field '{}'", field.getName());
                    String propertyName = field.getAnnotation(Value.class).value();
                    field.setAccessible(true);
                    if (!propertyName.isEmpty()) {
                        field.set(beanInstance, propertiesMap.get(propertyName));
                    } else {
                        field.set(beanInstance, propertiesMap.get(field.getName()));
                    }
                    log.debug("The field '{}' of bean '{}' has been set to '{}'", field.getName(),
                        beanInstance.getClass().getName(), field.get(beanInstance));
                }
            } catch (IllegalAccessException e) {
                throw new InitializePropertyException("Can't initialize property #" + field.getName());
            }
        }
    }

    private void initPropertiesMap() {
        var urlPath = ClassLoader.getSystemClassLoader().getResource("application.properties");
        if (urlPath != null) {
            var path = urlPath.getPath();
            try (var bufferedReader = new BufferedReader(new FileReader(path))) {
                Stream<String> lines = bufferedReader.lines();
                this.propertiesMap = lines
                        .map(line -> line.split("="))
                        .collect(toMap(arr -> arr[0].trim(), arr -> arr[1].trim()));
            } catch (IOException e) {
                //TODO
                // log.warn(application.properties wasn't found)
            }
        }
    }
}
