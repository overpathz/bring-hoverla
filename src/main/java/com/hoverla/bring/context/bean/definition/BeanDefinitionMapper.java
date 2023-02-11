package com.hoverla.bring.context.bean.definition;

import java.lang.reflect.Method;

/**
 * {@link  BeanDefinitionMapper} using to create {@link BeanDefinition}
 */
public class BeanDefinitionMapper {
    public BeanDefinition mapToBeanDefinition(Class<?> beanClass) {
        return new DefaultBeanDefinition(beanClass);
    }

    public BeanDefinition mapToBeanDefinition(Object configuration, Method beanMethod) {
        return new ConfigurationBeanDefinition(configuration, beanMethod);
    }
}