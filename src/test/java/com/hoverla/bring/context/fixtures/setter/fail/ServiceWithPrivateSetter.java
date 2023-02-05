package com.hoverla.bring.context.fixtures.setter.fail;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.context.fixtures.setter.success.MessageService;

@Bean
public class ServiceWithPrivateSetter {
    private MessageService messageService;

    @Autowired
    private void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    public MessageService getMessageService() {
        return messageService;
    }
}
