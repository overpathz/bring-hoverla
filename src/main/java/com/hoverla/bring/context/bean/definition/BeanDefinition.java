package com.hoverla.bring.context.bean.definition;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.annotation.Primary;
import com.hoverla.bring.context.bean.dependency.BeanDependency;

import java.util.Map;

/**
 * This class is needed for working with {@link Bean} elements.
 */
public interface BeanDefinition {
    /**
     *
     * @return Bean name
     */
    String name();

    /**
     *
     * @return Bean type(object.getClass())
     */
    Class<?> type();

    /**
     *
     * @return All beans related to this Bean Definition
     */
    Map<String, BeanDependency> dependencies();

    /**
     * @return true if instance initialized
     */
    boolean isInstantiated();

    /**
     * Initialized beans by bean definitiona
     */
    void instantiate(BeanDefinition... dependencies);

    /**
     *
     * @return Bean instance
     */
    Object getInstance();

    /**
     *
     * @return Determines if the current bean is {@link Primary}
     */
    boolean isPrimary();
}
