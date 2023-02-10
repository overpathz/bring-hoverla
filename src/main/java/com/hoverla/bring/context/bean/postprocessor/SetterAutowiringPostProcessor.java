package com.hoverla.bring.context.bean.postprocessor;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.context.ApplicationContext;
import com.hoverla.bring.exception.InvokeMethodException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Autowired annotation is applied to public methods which starts with 'set'
 */
public class SetterAutowiringPostProcessor implements PostProcessor {

    @Override
    public void process(Object beanInstance, ApplicationContext applicationContext) {
        for (Method method : beanInstance.getClass().getMethods()) {
            if (method.getName().startsWith("set") && method.isAnnotationPresent(Autowired.class)) {
                try {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Object[] arguments = new Object[parameterTypes.length];
                    for (int i = 0; i < parameterTypes.length; i++) {
                        var parameterType = parameterTypes[i];
                        arguments[i] = applicationContext.getBean(parameterType);
                    }
                    method.invoke(beanInstance, arguments);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new InvokeMethodException(String.format("Can't invoke '%s' method", method.getName()));
                }
            }
        }
    }
}
