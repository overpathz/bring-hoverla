package com.hoverla.bring.context.fixtures.setter.success;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.context.fixtures.setter.fail.MessageService;

@Bean
public class ServiceWithIncorrectSetterName {
    private com.hoverla.bring.context.fixtures.setter.fail.MessageService messageService;

    @Autowired
    public void initMessageService(com.hoverla.bring.context.fixtures.setter.fail.MessageService messageService) {
        this.messageService = messageService;
    }

    public MessageService getMessageService() {
        return messageService;
    }
}

