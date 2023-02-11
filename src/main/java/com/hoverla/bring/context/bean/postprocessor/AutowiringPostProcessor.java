package com.hoverla.bring.context.bean.postprocessor;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.context.ApplicationContext;
import com.hoverla.bring.exception.InitializePropertyException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

import static com.hoverla.bring.common.StringConstants.INITIALIZE_PROPERTY_EXCEPTION;

/**
 * AutowiringPostProcessor using to inject another bean to current by field.
 */
@Slf4j
public class AutowiringPostProcessor implements PostProcessor {

    @Override
    public void process(Object beanInstance, ApplicationContext applicationContext) {
        for (Field field : beanInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                log.debug("Injecting field '{}' to the bean of type {}", field.getName(), beanInstance.getClass());
                field.setAccessible(true);
                try {
                    field.set(beanInstance, applicationContext.getBean(field.getType()));
                } catch (IllegalAccessException e) {
                    String canNotInitializeMessage = INITIALIZE_PROPERTY_EXCEPTION;
                    throw new InitializePropertyException(canNotInitializeMessage + field.getName());
                } finally {
                    field.setAccessible(false);
                }
            }
        }
    }
}
