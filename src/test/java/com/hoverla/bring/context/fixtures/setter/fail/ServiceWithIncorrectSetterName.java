package com.hoverla.bring.context.fixtures.setter.fail;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.context.fixtures.setter.success.MessageService;

@Bean
public class ServiceWithIncorrectSetterName {
    private MessageService messageService;

    @Autowired
    public void initMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    public MessageService getMessageService() {
        return messageService;
    }
}

