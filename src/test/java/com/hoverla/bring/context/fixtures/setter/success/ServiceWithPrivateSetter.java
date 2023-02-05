package com.hoverla.bring.context.fixtures.setter.success;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.context.fixtures.setter.fail.MessageService;

@Bean
public class ServiceWithPrivateSetter {
    private com.hoverla.bring.context.fixtures.setter.fail.MessageService messageService;

    @Autowired
    private void setMessageService(com.hoverla.bring.context.fixtures.setter.fail.MessageService messageService) {
        this.messageService = messageService;
    }

    public MessageService getMessageService() {
        return messageService;
    }
}
