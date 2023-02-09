package com.hoverla.bring.context.bean.definition;

import com.hoverla.bring.context.bean.dependency.BeanDependency;

import java.util.Map;

public interface BeanDefinition {
    String name();

    Class<?> type();

    Map<String, BeanDependency> dependencies();

    boolean isInstantiated();

    void instantiate(BeanDefinition... dependencies);

    Object getInstance();

    boolean isPrimary();
}
