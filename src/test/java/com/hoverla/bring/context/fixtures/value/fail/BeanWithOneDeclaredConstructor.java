package com.hoverla.bring.context.fixtures.value.fail;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.annotation.Value;
import lombok.Getter;

@Bean
@Getter
public class BeanWithOneDeclaredConstructor {

    @Value
    private String message;

    public BeanWithOneDeclaredConstructor(int someId){}
}
