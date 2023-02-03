package com.hoverla.bring.context;


import com.hoverla.bring.annotation.Value;
import com.hoverla.bring.exception.InitializePropertyException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class ValueAnnotationProcessor implements PostProcessor {

    private Map<String, String> propertiesMap = new HashMap<>();

    public ValueAnnotationProcessor() {
        initPropertiesMap();
    }

    @Override
    public void process(Object beanInstance, ApplicationContext applicationContext) {
        for (Field field : beanInstance.getClass().getDeclaredFields()) {
            try {
                if (field.isAnnotationPresent(Value.class)) {
                    String propertyName = field.getAnnotation(Value.class).value();
                    field.setAccessible(true);
                    if (!propertyName.isEmpty()) {
                        field.set(beanInstance, propertiesMap.get(propertyName));
                    } else {
                        field.set(beanInstance, propertiesMap.get(field.getName()));
                    }
                }
            } catch (IllegalAccessException e) {
                throw new InitializePropertyException("Can't initialize property #" + field.getName());
            }
        }
    }

    private void initPropertiesMap() {
        try {
            var urlPath = ClassLoader.getSystemClassLoader().getResource("application.properties");
            if (urlPath != null) {
                var path = urlPath.getPath();
                Stream<String> lines = new BufferedReader(new FileReader(path)).lines();
                this.propertiesMap = lines
                        .map(line -> line.split("="))
                        .collect(toMap(arr -> arr[0].trim(), arr -> arr[1].trim()));
            }
        } catch (FileNotFoundException e) {
            //TODO
            // log.warn(application.properties wasn't found)
        }
    }
}
