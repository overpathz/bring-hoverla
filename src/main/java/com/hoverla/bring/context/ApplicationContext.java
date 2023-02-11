package com.hoverla.bring.context;

import com.hoverla.bring.exception.NoSuchBeanException;

import java.util.Map;

/**
 * ApplicationContext it's main API for working with bean's at runtime.
 * <p>
 * Example:
 *     private ApplicationContext getApplicationContext(String packageToScan) {
 *         return new AnnotationApplicationContextImpl(
 *             List.of(new BeanAnnotationScanner(new BeanDefinitionMapper(), packageToScan)),
 *             new BeanInitializer(new BeanDependencyNameResolver()));
 *     }
 */
public interface ApplicationContext {
    /**
     * @param beanType beanType(class.getClass())
     * @return Single bean by bean type If a bean doesn't find will be thrown {@link NoSuchBeanException}
     * @param <T> Bean type
     */
    <T> T getBean(Class<T> beanType);

    /**
     * @param name Class name
     * @param beanType bean type(class.getClass())
     * @return Single bean by bean type If a bean doesn't find will be thrown {@link NoSuchBeanException}
     * @param <T> Bean type
     */

    <T> T getBean(String name, Class<T> beanType);

    /**
     * @param beanType bean type(class.getClass())
     * @return All beans by bean type
     * @param <T> Bean type
     */
    <T> Map<String, T> getAllBeans(Class<T> beanType);
}