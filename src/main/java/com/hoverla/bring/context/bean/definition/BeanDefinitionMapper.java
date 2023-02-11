package com.hoverla.bring.context.bean.definition;

import java.lang.reflect.Method;

public class BeanDefinitionMapper {

    public BeanDefinition mapToBeanDefinition(Class<?> beanClass) {
        return new DefaultBeanDefinition(beanClass);
    }

    public BeanDefinition mapToBeanDefinition(Object configuration, Method beanMethod) {
        return new ConfigurationBeanDefinition(configuration, beanMethod);
    }
}