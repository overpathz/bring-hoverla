package com.hoverla.bring.context;


import com.hoverla.bring.annotation.Value;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class ValueAnnotationProcessor implements PostProcessor{

    private Map<String, String> propertiesMap;

    public ValueAnnotationProcessor() {
        initPropertiesMap();
    }

    @Override
    public void process(Object beanInstance, ApplicationContext applicationContext) {
        try {
            for (Field field : beanInstance.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Value.class)) {
                    String propertyName = field.getAnnotation(Value.class).value();
                    field.setAccessible(true);
                    if (!propertyName.isEmpty()) {
                        field.set(beanInstance, propertiesMap.get(propertyName));
                    } else {
                        field.set(beanInstance, propertiesMap.get(field.getName()));
                    }

                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void initPropertiesMap() {
        try {
            String path = ClassLoader.getSystemClassLoader().getResource("application.properties").getPath();
            Stream<String> lines = new BufferedReader(new FileReader(path)).lines();
            this.propertiesMap = lines
                    .map(line -> line.split("="))
                    .collect(toMap(arr -> arr[0].trim(), arr -> arr[1].trim()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Application properties wasn't found");
        }
    }
}
