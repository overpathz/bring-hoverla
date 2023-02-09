package com.hoverla.bring.context.bean.scanner;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.annotation.Configuration;
import com.hoverla.bring.context.bean.definition.BeanDefinitionMapper;
import com.hoverla.bring.context.bean.definition.BeanDefinition;
import lombok.SneakyThrows;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ConfigurationBeanScanner implements BeanScanner {
    private final String[] packagesToScan;
    private final BeanDefinitionMapper mapper;

    public ConfigurationBeanScanner(BeanDefinitionMapper mapper,
                                         String... packagesToScan) {
        this.packagesToScan = packagesToScan;
        this.mapper = mapper;
    }

    @Override
    public List<BeanDefinition> scan() {
        Reflections reflections = new Reflections((Object[]) packagesToScan);
        Set<Class<?>> configurationClasses = reflections.getTypesAnnotatedWith(Configuration.class);

        if (configurationClasses.isEmpty()) {
            return Collections.emptyList();
        }

        return configurationClasses.stream()
            .map(this::scanBeanConfigMethods)
            .flatMap(List::stream)
            .collect(toList());
    }

    private List<BeanDefinition> scanBeanConfigMethods(Class<?> configurationClass) {
        //TODO add validation for @Configuration class
        Object configuration = createConfigurationInstance(configurationClass);

        return resolveBeanMethods(configurationClass).stream()
            .map(method -> mapper.mapToBeanDefinition(configuration, method))
            .collect(toList());
    }

    private List<Method> resolveBeanMethods(Class<?> configClass) {
        return Stream.of(configClass.getMethods())
            .filter(m -> m.isAnnotationPresent(Bean.class))
            .collect(toList());
    }

    @SneakyThrows
    private Object createConfigurationInstance(Class<?> configClass) {
        return configClass.getConstructor().newInstance();
    }
}
