package com.hoverla.bring.context;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.exception.InitializePropertyException;

import java.lang.reflect.Field;

public class AutowiringPostProcessor implements PostProcessor {

    @Override
    public void process(Object beanInstance, ApplicationContext applicationContext) {

        for (Field field : beanInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                try {
                    field.set(beanInstance, applicationContext.getBean(field.getType()));
                } catch (IllegalAccessException e) {
                    throw new InitializePropertyException("Can't initialize property #" + field.getName());
                }
            }

        }
    }
}
