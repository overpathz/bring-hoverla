package com.hoverla.bring.context.fixtures.bean.primary.error;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.annotation.Primary;
import com.hoverla.bring.context.fixtures.bean.success.A;

@Bean
@Primary
public class WolfError implements AnimalError {
}
