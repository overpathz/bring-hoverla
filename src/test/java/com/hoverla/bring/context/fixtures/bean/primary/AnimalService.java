package com.hoverla.bring.context.fixtures.bean.primary;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.annotation.Bean;

@Bean
public class AnimalService {
    @Autowired
    public Animal animal;
}
