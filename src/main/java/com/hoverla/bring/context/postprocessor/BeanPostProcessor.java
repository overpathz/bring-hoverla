package com.hoverla.bring.context.postprocessor;

import com.hoverla.bring.context.ApplicationContext;

public interface BeanPostProcessor {
    void process(Object bean, ApplicationContext applicationContext);
}
