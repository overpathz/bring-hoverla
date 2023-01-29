package com.hoverla.bring.context;

import java.util.Map;

public interface ApplicationContext {

    <T> T getBean(Class<T> beanType);

    <T> T getBean(String name, Class<T> beanType);

    <T> Map<String, T> getAllBeans(Class<T> beanType);
}
