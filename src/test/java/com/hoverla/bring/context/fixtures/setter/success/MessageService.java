package com.hoverla.bring.context.fixtures.setter.success;

import com.hoverla.bring.annotation.Bean;

@Bean
public class MessageService {

    public String getMessage(){
        return "some text";
    }
}
