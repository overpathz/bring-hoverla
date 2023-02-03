package com.hoverla.bring.context.fixtures.value.success;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.annotation.Value;
import lombok.Getter;

@Getter
@Bean("beanWithValue")
public class BeanWithValueAnnotation {

    @Value
    private String message;

    @Value("value.message")
    private String valueMessage;

}
