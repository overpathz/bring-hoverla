package com.hoverla.bring.context.bean.postprocessor;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.context.ApplicationContext;
import com.hoverla.bring.exception.InitializePropertyException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public class AutowiringPostProcessor implements PostProcessor {

    @Override
    @SuppressWarnings("java:S3011")
    public void process(Object beanInstance, ApplicationContext applicationContext) {
        for (Field field : beanInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                log.debug("Injecting field '{}' to the bean of type {}", field.getName(), beanInstance.getClass());
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
