package com.hoverla.bring.context.bean.definition;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.annotation.Configuration;
import com.hoverla.bring.annotation.Primary;
import com.hoverla.bring.context.bean.dependency.BeanDependency;
import com.hoverla.bring.context.util.ResolveDependenciesUtil;
import com.hoverla.bring.exception.BeanInstanceCreationException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.hoverla.bring.common.StringConstants.*;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * This class describes the internals of a {@link Bean} created by {@link Configuration}
 * and provides a basic BeanDefinition implementation.
 * <p>
 * @see Configuration
 */
@Slf4j
public class ConfigurationBeanDefinition extends AbstractBeanDefinition {
    private final Object configInstance;
    private final Method beanMethod;

    public ConfigurationBeanDefinition(Object configInstance, Method beanMethod) {
        Objects.requireNonNull(configInstance, CONFIGURATION_CLASS_INSTANCE_ERROR_MESSAGE);
        Objects.requireNonNull(beanMethod, CONFIGURATION_BEAN_METHOD_ERROR_MESSAGE);
        log.debug("Creating the bean definition from method '{}'", beanMethod);

        this.configInstance = configInstance;
        this.beanMethod = beanMethod;

        this.name = resolveName(beanMethod);
        log.trace("Bean name is '{}'", name);

        this.type = getType(beanMethod);
        log.trace("'{}' bean type is '{}'", name, type);

        this.dependencies = resolveDependencies(beanMethod);
        log.trace("'{}' bean dependencies are {}", name, dependencies);
    }

    @Override
    public void instantiate(BeanDefinition... dependencies) {
        if (!isInstantiated()) {
            log.debug("Instantiating bean: '{}' of type {}", name, type.getName());
            Map<String, Class<?>> dependenciesMap = Stream.of(dependencies)
                .collect(toMap(BeanDefinition::name, BeanDefinition::type));
            log.trace("Bean '{}' of type {} has the following dependencies: {}", name, type.getName(), dependenciesMap);
            List<BeanDefinition> dependencyList = new ArrayList<>(List.of(dependencies));
            instance = createInstance(dependencyList);
            log.debug("Bean '{}' of type {} has been instantiated", name, type.getName());
        }
    }

    @Override
    public boolean isPrimary() {
        return beanMethod.isAnnotationPresent(Primary.class);
    }

    private String resolveName(Method beanMethod) {
        Bean beanAnnotation = beanMethod.getAnnotation(Bean.class);
        String beanName = beanAnnotation.value();
        if (isBlank(beanName)) {
            return beanMethod.getName();
        }
        return beanName;
    }

    private Class<?> getType(Method beanMethod) {
        return beanMethod.getReturnType();
    }

    private Map<String, BeanDependency> resolveDependencies(Method beanMethod) {
        return Stream.of(beanMethod.getParameters())
                .map(BeanDependency::fromParameter)
                .collect(toMap(BeanDependency::getName, Function.identity()));
    }

    private Object createInstance(List<BeanDefinition> dependencies) {
        try {
            Parameter[] parameters = beanMethod.getParameters();
            Object[] constructorArguments = new Object[parameters.length];

            List<Parameter> params = new ArrayList<>(List.of(parameters));
            ResolveDependenciesUtil.resolveDependencies(dependencies, params, constructorArguments, name);
            return beanMethod.invoke(configInstance, constructorArguments);
        } catch (Exception e) {
            throw new BeanInstanceCreationException(String.format(BEAN_INSTANCE_CREATION_EXCEPTION, name), e);
        }
    }
}