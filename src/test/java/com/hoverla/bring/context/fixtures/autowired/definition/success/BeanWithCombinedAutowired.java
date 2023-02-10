package com.hoverla.bring.context.fixtures.autowired.definition.success;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.annotation.Bean;
import lombok.Getter;

@Bean
public class BeanWithCombinedAutowired {

    @Getter
    private final BeanWithAutowiredConstructor beanWithAutowiredConstructor;

    @Getter
    @Autowired
    private BeanWithConstructor beanWithConstructor;


    @Autowired
    public BeanWithCombinedAutowired(BeanWithAutowiredConstructor beanWithAutowiredConstructor) {
        this.beanWithAutowiredConstructor = beanWithAutowiredConstructor;
    }
}
