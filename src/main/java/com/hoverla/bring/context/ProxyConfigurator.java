package com.hoverla.bring.context;

public interface ProxyConfigurator {
    Object replaceWithProxyIfNeeded(Object t, Class<?> implClass);
}
