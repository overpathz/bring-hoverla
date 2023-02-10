package com.hoverla.bring.context.fixtures.autowired.definition.success;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.context.fixtures.bean.success.TestBeanWithoutName;
import lombok.Getter;

@Bean
public class BeanWithMultipleAutowiredFields {

    @Autowired
    @Getter
    private TestBeanWithoutName testBeanWithoutName;

    @Autowired
    @Getter
    String string;

    @Autowired
    @Getter
    Double aDouble;
}
