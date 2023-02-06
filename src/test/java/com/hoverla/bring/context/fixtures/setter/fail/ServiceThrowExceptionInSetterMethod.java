package com.hoverla.bring.context.fixtures.setter.fail;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.annotation.Bean;

@Bean
public class ServiceThrowExceptionInSetterMethod {
    private MessageService messageService;

    @Autowired
    public void setMessageService(MessageService messageService) {
        int a = 1/0;
        this.messageService = messageService;
    }

    public MessageService getMessageService() {
        return messageService;
    }
}
