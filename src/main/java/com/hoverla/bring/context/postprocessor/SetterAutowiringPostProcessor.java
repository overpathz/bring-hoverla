package com.hoverla.bring.context.postprocessor;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.context.ApplicationContext;
import com.hoverla.bring.exception.InvokeMethodException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SetterAutowiringPostProcessor implements PostProcessor {

    @Override
    public void process(Object beanInstance, ApplicationContext applicationContext) {
        for (Method method : beanInstance.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("set") && method.isAnnotationPresent(Autowired.class)) {
                method.setAccessible(true);
                try {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Object[] arguments = new Object[parameterTypes.length];
                    for (int i = 0; i < parameterTypes.length; i++) {
                        var parameterType = parameterTypes[i];
                        arguments[i] = applicationContext.getBean(parameterType);
                    }
                    method.invoke(beanInstance, arguments);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new InvokeMethodException("Can't invoke method #" + method.getName());
                }
            }
        }
    }
}
