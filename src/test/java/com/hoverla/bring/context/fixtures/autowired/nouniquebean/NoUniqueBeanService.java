package com.hoverla.bring.context.fixtures.autowired.nouniquebean;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.annotation.Bean;

@Bean
public class NoUniqueBeanService {

    @Autowired
    JustAnotherService justAnotherService;
}
