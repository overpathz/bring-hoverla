package com.hoverla.bring.context.fixtures.setter.success;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.annotation.Bean;

@Bean
public class Container {

    private MessageService messageService;

    private NumberService numberService;

    @Autowired
    public void setNumberServiceAndMessageService(MessageService messageService, NumberService numberService){
        this.messageService = messageService;
        this.numberService = numberService;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public NumberService getNumberService() {
        return numberService;
    }
}
