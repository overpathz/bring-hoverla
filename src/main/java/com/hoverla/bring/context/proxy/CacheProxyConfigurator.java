package com.hoverla.bring.context.proxy;

import com.hoverla.bring.annotation.Cacheable;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheProxyConfigurator implements ProxyConfigurator {

    private final Map<String, Map<List<Object>, Object>> cache = new ConcurrentHashMap<>();

    @Override
    public Object replaceWithProxyIfNeeded(Object bean, Class<?> type) {
        boolean isCacheable = Arrays.stream(type.getMethods()).anyMatch(m -> m.isAnnotationPresent(Cacheable.class));
        if (isCacheable) {
            MethodInterceptor methodInterceptor = (obj, method, args, proxy) -> {
                if (method.isAnnotationPresent(Cacheable.class)) {
                    String cacheName = method.getAnnotation(Cacheable.class).value();
                    return getCacheData(method, args, bean, cacheName);
                }
                return method.invoke(bean, args);
            };
            return Enhancer.create(type, methodInterceptor);
        }
        return bean;
    }

    private Object getCacheData(Method method, Object[] args, Object bean, String cacheName) throws IllegalAccessException, InvocationTargetException {
        var argumentsKey = List.of(args);
        if (cache.containsKey(cacheName)) {
            Map<List<Object>, Object> argumentsMap = cache.get(cacheName);
            if (argumentsMap.containsKey(argumentsKey)) {
                return argumentsMap.get(argumentsKey);
            }
            var result = method.invoke(bean, args);
            argumentsMap.put(argumentsKey, result);
            return result;
        }
        var result = method.invoke(bean, args);
        Map<List<Object>, Object> argumentsMap = new HashMap<>();
        argumentsMap.put(argumentsKey, result);
        cache.put(cacheName, argumentsMap);
        return result;
    }

}
