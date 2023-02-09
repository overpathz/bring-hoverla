package com.hoverla.bring.context;

import com.hoverla.bring.context.fixtures.setter.success.Container;
import com.hoverla.bring.context.fixtures.setter.success.MessageServiceHolder;
import com.hoverla.bring.context.fixtures.setter.success.ServiceWithIncorrectSetterName;
import com.hoverla.bring.context.fixtures.setter.success.ServiceWithPrivateSetter;
import com.hoverla.bring.exception.InvokeMethodException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SetterInjectBeanPostProcessorTest {

    @Test
    @Order(1)
    @DisplayName("Setter autowiring has been successful")
    void autowiringSetterWorksProperly() {
        ApplicationContext autowiringContext =
                new AnnotationApplicationContextImpl("com.hoverla.bring.context.fixtures.setter.success");
        MessageServiceHolder holder = autowiringContext.getBean(MessageServiceHolder.class);
        assertEquals("some text", holder.getMessageService().getMessage());
    }

    @Test
    @Order(2)
    @DisplayName("Setter with 2 arguments has been successful")
    void autowiringSetterWith2ArgumentsWorksProperly() {
        ApplicationContext autowiringContext =
                new AnnotationApplicationContextImpl("com.hoverla.bring.context.fixtures.setter.success");
        Container container = autowiringContext.getBean(Container.class);
        assertEquals("some text", container.getMessageService().getMessage());
        assertEquals(2, container.getNumberService().getNumber());
    }

    @Test
    @Order(3)
    @DisplayName("Setter with private modified hasn't been successful")
    void autowiringSetterDoesntWorkForPrivateSetter() {
        ApplicationContext autowiringContext =
                new AnnotationApplicationContextImpl("com.hoverla.bring.context.fixtures.setter.success");
        ServiceWithPrivateSetter bean = autowiringContext.getBean(ServiceWithPrivateSetter.class);
        assertNull(bean.getMessageService());
    }

    @Test
    @Order(4)
    @DisplayName("Setter with with incorrect name hasn't been successful")
    void autowiringSetterDoesntWorkForIncorrectSetterName() {
        ApplicationContext autowiringContext =
                new AnnotationApplicationContextImpl("com.hoverla.bring.context.fixtures.setter.success");
        ServiceWithIncorrectSetterName bean = autowiringContext.getBean(ServiceWithIncorrectSetterName.class);
        assertNull(bean.getMessageService());
    }

    @Test
    @Order(5)
    @DisplayName("Setter throw invokeMethodException")
    void autowiringSetterThrowInvokeMethodException() {
        InvokeMethodException exception = assertThrows(InvokeMethodException.class, () ->
                new AnnotationApplicationContextImpl("com.hoverla.bring.context.fixtures.setter.fail"));
        assertEquals("Can't invoke 'setMessageService' method", exception.getMessage());
    }
}
