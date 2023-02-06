package com.hoverla.bring.context.fixtures.setter.fail;

import com.hoverla.bring.annotation.Bean;

@Bean
public class MessageService {

    public String getMessage(){
        return "some text";
    }
}
