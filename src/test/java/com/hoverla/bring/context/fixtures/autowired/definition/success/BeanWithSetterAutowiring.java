package com.hoverla.bring.context.fixtures.autowired.definition.success;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.annotation.Bean;

@Bean
public class BeanWithSetterAutowiring {

    @Autowired
    BeanWithAutowiredConstructor beanWithAutowiredConstructor;

    @Autowired
    BeanWithConstructor beanWithConstructor;

    @Autowired
    Double aDouble;
}
