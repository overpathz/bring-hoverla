package com.hoverla.bring.context.fixtures.initFailure;

import com.hoverla.bring.annotation.Bean;

@Bean
public class ClassWithPrivateConstructor {

    private ClassWithPrivateConstructor() {

    }
}
