package com.hoverla.bring.context.fixtures.value;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.annotation.Value;
import lombok.Getter;

@Getter
@Bean("beanWithValue")
public class ClassWithValueAnnotation {

    @Value
    private String message;

    @Value("value.message")
    private String valueMessage;

}
