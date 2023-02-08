package com.hoverla.bring.context.fixtures.bean.primary;

import com.hoverla.bring.annotation.Bean;

@Bean
public class Wolf implements Animal {
    @Override
    public int strongPoints() {
        return 70;
    }
}
