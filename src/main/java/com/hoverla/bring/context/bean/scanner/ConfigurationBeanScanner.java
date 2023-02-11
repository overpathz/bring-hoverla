package com.hoverla.bring.context.bean.scanner;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.annotation.Configuration;
import com.hoverla.bring.context.ApplicationContext;
import com.hoverla.bring.context.bean.definition.BeanDefinitionMapper;
import com.hoverla.bring.context.bean.definition.BeanDefinition;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * *{@link BeanAnnotationScanner} using for scan packages to find classes annotated with {@link Configuration}
 * and create it at {@link ApplicationContext}
 *
 * @see Bean
 */
@Slf4j
public class ConfigurationBeanScanner implements BeanScanner {
    private final String[] packagesToScan;
    private final BeanDefinitionMapper mapper;

    public ConfigurationBeanScanner(BeanDefinitionMapper mapper, String... packagesToScan) {
        this.packagesToScan = packagesToScan;
        this.mapper = mapper;
    }

    @Override
    public List<BeanDefinition> scan() {
        log.info("Starting the scan process of classes annotated with '@Configuration' within the '{}' packages",
            Arrays.toString(this.packagesToScan));

        Reflections reflections = new Reflections((Object[]) packagesToScan);
        Set<Class<?>> configurationClasses = reflections.getTypesAnnotatedWith(Configuration.class);

        if (configurationClasses.isEmpty()) {
            log.warn("No classes annotated with '@Configuration' found during the scan in packages {}",
                Arrays.toString(this.packagesToScan));
            return Collections.emptyList();
        }
        log.debug("{} classes annotated with '@Configuration' have been found", configurationClasses.size());

        return configurationClasses.stream()
                .map(this::scanBeanConfigMethods)
                .flatMap(List::stream)
                .collect(toList());
    }

    private List<BeanDefinition> scanBeanConfigMethods(Class<?> configurationClass) {
        //TODO add validation for @Configuration class
        Object configurationInstance = createConfigurationInstance(configurationClass);

        return resolveBeanMethods(configurationClass).stream()
                .map(method -> mapper.mapToBeanDefinition(configurationInstance, method))
                .collect(toList());
    }

    private List<Method> resolveBeanMethods(Class<?> configClass) {
        return Stream.of(configClass.getMethods())
                .filter(method -> method.isAnnotationPresent(Bean.class))
                .collect(toList());
    }

    @SneakyThrows
    private Object createConfigurationInstance(Class<?> configClass) {
        return configClass.getConstructor().newInstance();
    }
}
