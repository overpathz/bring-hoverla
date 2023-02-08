package com.hoverla.bring.context.proxy;

public interface ProxyConfigurator {
    Object replaceWithProxyIfNeeded(Object t, Class<?> implClass);
}
