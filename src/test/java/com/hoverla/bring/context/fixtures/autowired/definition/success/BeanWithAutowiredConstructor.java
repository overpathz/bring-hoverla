package com.hoverla.bring.context.fixtures.autowired.definition.success;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.annotation.Bean;
import lombok.Getter;

import java.time.LocalDate;

@Bean
public class BeanWithAutowiredConstructor {
    @Getter
    private final String string;
    @Getter
    private final LocalDate date;

    @Autowired
    public BeanWithAutowiredConstructor(String string, LocalDate date) {
        this.string = string;
        this.date = date;
    }
}
