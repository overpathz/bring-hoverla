package com.hoverla.bring.context.bean.scanner;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.context.ApplicationContext;
import com.hoverla.bring.context.bean.definition.BeanDefinition;

import java.util.List;

/**
 * {@link BeanScanner} using for scan packages to find classes and create it at {@link ApplicationContext}
 * @see Bean
 */
public interface BeanScanner {
    List<BeanDefinition> scan();
}