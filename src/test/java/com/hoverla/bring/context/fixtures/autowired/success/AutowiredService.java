package com.hoverla.bring.context.fixtures.autowired.success;

import com.hoverla.bring.annotation.Bean;

import java.util.List;

@Bean
public class AutowiredService extends AutowiringParentService {

    public List<String> getLettersList() {
        return List.of("A", "B", "C");
    }
}
