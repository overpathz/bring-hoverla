package com.hoverla.bring.context.postprocessor;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.context.ApplicationContext;
import com.hoverla.bring.exception.InitializePropertyException;

import java.lang.reflect.Field;

public class AutowiringPostProcessor implements PostProcessor {

    @Override
    public void process(Object beanInstance, ApplicationContext applicationContext) {
        for (Field field : beanInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                try {
                    //TODO add constuctor/setter injection
                    field.set(beanInstance, applicationContext.getBean(field.getType()));
                } catch (IllegalAccessException e) {
                    String canNotInitializeMessage = InitializePropertyException.INITIALIZE_PROPERTY_EXCEPTION;
                    throw new InitializePropertyException(canNotInitializeMessage + field.getName());
                } finally {
                    field.setAccessible(false);
                }
            }
        }
    }
}
