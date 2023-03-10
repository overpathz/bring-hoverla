package com.hoverla.bring.context.bean.postprocessor;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.context.ApplicationContext;
import com.hoverla.bring.exception.InvokeMethodException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static com.hoverla.bring.common.StringConstants.INVOKE_METHOD_EXCEPTION;

/**
 * SetterAutowiringPostProcessor using to inject another bean to current by setter.
 * @see Bean
 * <p>
 * Autowired annotation is applied to public methods which starts with 'set'
 */
@Slf4j
public class SetterAutowiringBeanPostProcessor implements BeanPostProcessor {

    @Override
    public void process(Object beanInstance, ApplicationContext applicationContext) {
        for (Method method : beanInstance.getClass().getMethods()) {
            if (method.getName().startsWith("set") && method.isAnnotationPresent(Autowired.class)) {
                log.trace("Trying to inject fields from method '{}'", method.getName());
                try {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    log.debug("Injecting field(s) '{}' to the bean of type {}", Arrays.toString(parameterTypes),
                        beanInstance.getClass());
                    Object[] arguments = new Object[parameterTypes.length];
                    for (int i = 0; i < parameterTypes.length; i++) {
                        var parameterType = parameterTypes[i];
                        arguments[i] = applicationContext.getBean(parameterType);
                    }
                    method.invoke(beanInstance, arguments);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new InvokeMethodException(String.format(INVOKE_METHOD_EXCEPTION, method.getName()));
                }
            }
        }
    }
}
