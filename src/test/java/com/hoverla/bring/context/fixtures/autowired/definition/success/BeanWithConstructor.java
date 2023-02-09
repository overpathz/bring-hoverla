package com.hoverla.bring.context.fixtures.autowired.definition.success;

import com.hoverla.bring.annotation.Bean;
import lombok.Getter;

import java.time.LocalDate;

@Bean("someBean")
public class BeanWithConstructor {
    @Getter
    private final String string;
    @Getter
    private final LocalDate date;

    public BeanWithConstructor(String string, LocalDate date) {
        this.string = string;
        this.date = date;
    }
}
