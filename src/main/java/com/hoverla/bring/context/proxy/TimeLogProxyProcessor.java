package com.hoverla.bring.context.proxy;

import com.hoverla.bring.annotation.Cacheable;
import com.hoverla.bring.annotation.TimeLog;
import com.hoverla.bring.context.AnnotationApplicationContextImpl;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class TimeLogProxyProcessor implements ProxyConfigurator {
    Logger log = LoggerFactory.getLogger(TimeLogProxyProcessor.class);

    @Override
    public Object replaceWithProxyIfNeeded(Object bean, Class<?> type) {
        boolean isTimeLogged = Arrays.stream(type.getMethods()).anyMatch(m -> m.isAnnotationPresent(TimeLog.class));
        if (isTimeLogged) {
            MethodInterceptor methodInterceptor = (obj, method, args, proxy) -> {
                if (method.isAnnotationPresent(TimeLog.class)) {
                    long startTime = System.nanoTime();
                    Object result = method.invoke(bean, args);
                    long duration = (System.nanoTime() - startTime) / 1_000;
                    log.info(String.format("The time execution of '%s' method takes %dms", method.getName(), duration));
                    return result;
                }
                return method.invoke(bean, args);
            };
            return Enhancer.create(type, methodInterceptor);
        }
        return bean;
    }
}
