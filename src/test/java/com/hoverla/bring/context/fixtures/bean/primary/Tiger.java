package com.hoverla.bring.context.fixtures.bean.primary;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.annotation.Primary;

@Bean
@Primary
public class Tiger implements Animal {
    @Override
    public int strongPoints() {
        return 100;
    }
}
