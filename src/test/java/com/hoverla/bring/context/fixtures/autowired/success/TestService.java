package com.hoverla.bring.context.fixtures.autowired.success;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.annotation.Bean;

@Bean("NotDefaultName")
public class TestService extends AutowiringParentService {
    @Autowired
    private AutowiredService autowiredService;

    public String getLetters() {
        return String.join(",", autowiredService.getLettersList());
    }
}
