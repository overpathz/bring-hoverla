package com.hoverla.bring.context.bean.postprocessor;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.context.ApplicationContext;

/**
 * PostProcessor using for additional configuration {@link Bean}
 * @see Bean
 */
public interface PostProcessor {
    /**
     * Process additional configuration for bean.
     */
    void process(Object bean, ApplicationContext applicationContext);
}
