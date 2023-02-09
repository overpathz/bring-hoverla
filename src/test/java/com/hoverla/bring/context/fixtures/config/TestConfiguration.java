package com.hoverla.bring.context.fixtures.config;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.annotation.Configuration;

@Configuration
public class TestConfiguration {
    @Bean
    public String unnamedBean() {
        return "string";
    }

    @Bean("stringBean")
    public String namedBean() {
        return "namedString";
    }

    @Bean
    public Long beanWithDependencies(long n, String s) {
        return n - Long.parseLong(s);
    }

    @Bean
    public Long anotherBeanWithDependencies(long n, String s, String s1) {
        return n - Long.parseLong(s) + Long.parseLong(s1);
    }

    public String notABean() {
        return "I'm not a bean.";
    }

}
