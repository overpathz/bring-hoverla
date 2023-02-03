package com.hoverla.bring.context.fixtures.autowired.nosuchbean;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.annotation.Bean;

@Bean
public class NoSuchBeanService {

    @Autowired
    NotABeanService notABeanService;
}
