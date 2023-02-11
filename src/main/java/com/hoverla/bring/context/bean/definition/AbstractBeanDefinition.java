package com.hoverla.bring.context.bean.definition;

import com.hoverla.bring.context.bean.dependency.BeanDependency;

import java.util.Map;
import java.util.Objects;

public abstract class AbstractBeanDefinition implements BeanDefinition {
    protected Object instance;
    protected String name;
    protected Class<?> type;
    protected Map<String, BeanDependency> dependencies;

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
    public Object getInstance() {
        return instance;
    }
}