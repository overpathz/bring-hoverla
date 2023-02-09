package com.hoverla.bring.context.bean.definition;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.annotation.Primary;
import com.hoverla.bring.context.bean.dependency.BeanDependency;
import com.hoverla.bring.context.util.ResolveDependenciesUtil;
import com.hoverla.bring.exception.BeanInstanceCreationException;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class ConfigurationBeanDefinition implements BeanDefinition {
    private Object instance;
    private final String name;
    private final Object configInstance;
    private final Method beanMethod;
    private final Class<?> type;
    private final Map<String, BeanDependency> dependencies;

    public ConfigurationBeanDefinition(Object configInstance, Method beanMethod) {
        Objects.requireNonNull(configInstance, "Configuration class instance can't be null");
        Objects.requireNonNull(beanMethod, "Configuration bean method can't be null");

        this.configInstance = configInstance;
        this.beanMethod = beanMethod;
        this.name = resolveName(beanMethod);
        this.type = getType(beanMethod);
        this.dependencies = resolveDependencies(beanMethod);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<?> type() {
        return type;
    }

    @Override
    public Map<String, BeanDependency> dependencies() {
        return dependencies;
    }

    @Override
    public boolean isInstantiated() {
        return Objects.nonNull(instance);
    }

    @Override
    public void instantiate(BeanDefinition... dependencies) {
        if (!isInstantiated()) {
            List<BeanDefinition> dependencyList = new ArrayList<>(List.of(dependencies));
            instance = createInstance(dependencyList);
        }
    }

    @Override
    public Object getInstance() {
        return instance;
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
            throw new BeanInstanceCreationException(String.format("Bean with name '%s' can't be instantiated", name), e);
        }
    }
}
