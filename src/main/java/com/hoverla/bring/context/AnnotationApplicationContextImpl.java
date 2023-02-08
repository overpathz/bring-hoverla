package com.hoverla.bring.context;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.annotation.Primary;
import com.hoverla.bring.context.postprocessor.PostProcessor;
import com.hoverla.bring.context.proxy.ProxyConfigurator;
import com.hoverla.bring.exception.ApplicationContextInitializationException;
import com.hoverla.bring.exception.DefaultConstructorNotFoundException;
import com.hoverla.bring.exception.NoSuchBeanException;
import com.hoverla.bring.exception.NoUniqueBeanException;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.hoverla.bring.exception.ApplicationContextInitializationException.APPLICATION_INITIALIZATION_EXCEPTION;
import static com.hoverla.bring.exception.DefaultConstructorNotFoundException.DEFAULT_CONSTRUCTOR_NOT_FOUND_EXCEPTION;
import static com.hoverla.bring.exception.NoSuchBeanException.NO_SUCH_BEAN_EXCEPTION_BY_NAME_TYPE;
import static com.hoverla.bring.exception.NoSuchBeanException.NO_SUCH_BEAN_EXCEPTION_BY_TYPE;
import static com.hoverla.bring.exception.NoUniqueBeanException.NO_UNIQUE_BEAN_EXCEPTION;
import static com.hoverla.bring.exception.NoUniqueBeanException.NO_UNIQUE_PRIMARY_BEAN_EXCEPTION;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class AnnotationApplicationContextImpl implements ApplicationContext {

    Logger log = LoggerFactory.getLogger(AnnotationApplicationContextImpl.class);
    private final Map<String, Object> beans = new ConcurrentHashMap<>();
    private final List<PostProcessor> postProcessors = new ArrayList<>();
    private final List<ProxyConfigurator> proxyConfigurators = new ArrayList<>();

    private static final String BASE_BRING_PACKAGE = "com.hoverla.bring";

    public AnnotationApplicationContextImpl(String... basePackages) {
        Reflections beanScanner = new Reflections((Object[]) basePackages);
        Set<Class<?>> beanClasses = beanScanner.getTypesAnnotatedWith(Bean.class, true);

        if (beanClasses.isEmpty()) {
            return;
        }
        initProxyConfigurators();
        try {
            initBeans(beanClasses);
            postProcess();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ApplicationContextInitializationException(format(APPLICATION_INITIALIZATION_EXCEPTION, e.getMessage()));
        }
    }

    @Override
    public <T> T getBean(Class<T> beanType) {
        Map<String, T> beanMap = getAllBeans(beanType);

        if (beanMap.size() > 1) {
            return getPrimaryBean(beanMap, beanType);
        }

        return beanMap.entrySet().stream().findFirst()
                .map(Entry::getValue)
                .orElseThrow(() -> new NoSuchBeanException(format(NO_SUCH_BEAN_EXCEPTION_BY_TYPE, beanType.getSimpleName())));
    }

    private Object mapToProxy(Object bean, Class<?> beanType) {
        for (ProxyConfigurator proxyConfigurator : proxyConfigurators) {
            bean = proxyConfigurator.replaceWithProxyIfNeeded(bean, beanType);
        }
        return bean;
    }

    @Override
    public <T> T getBean(String name, Class<T> beanType) {
        return Optional.ofNullable(beans.get(name))
                .filter(potentialBean -> beanType.isAssignableFrom(potentialBean.getClass()))
                .map(beanType::cast)
                .orElseThrow(() -> new NoSuchBeanException(format(NO_SUCH_BEAN_EXCEPTION_BY_NAME_TYPE, name, beanType.getSimpleName())));
    }

    @Override
    public <T> Map<String, T> getAllBeans(Class<T> beanType) {
        return beans.entrySet().stream()
                .filter(potentialBean -> beanType.isAssignableFrom(potentialBean.getValue().getClass()))
                .collect(toMap(Entry::getKey, bean -> beanType.cast(bean.getValue())));
    }

    private void initBeans(Set<Class<?>> beanClasses) throws InstantiationException, IllegalAccessException {
        for (Class<?> beanType : beanClasses) {
            String beanName = resolveBeanName(beanType);
            Object instance;
            try {
                instance = beanType.getConstructor().newInstance();
                instance = mapToProxy(instance, beanType);
            } catch (InvocationTargetException | NoSuchMethodException e) {
                throw new DefaultConstructorNotFoundException(format(DEFAULT_CONSTRUCTOR_NOT_FOUND_EXCEPTION, beanType.getSimpleName()));
            }
            beans.put(beanName, instance);
        }
    }

    private String resolveBeanName(Class<?> type) {
        String beanName = type.getAnnotation(Bean.class).value();
        return isNotBlank(beanName) ? beanName : resolveBeanName(type.getSimpleName());
    }

    private String resolveBeanName(String typeName) {
        return typeName.substring(0, 1).toLowerCase() + typeName.substring(1);
    }

    @SuppressWarnings("java:S3011")
    private void postProcess() throws IllegalAccessException {
        initPostProcessors();
        Collection<Object> beanInstances = beans.values();
        for (Object beanInstance : beanInstances) {
            postProcessors.forEach(postProcessor -> postProcessor.process(beanInstance, this));
        }
    }

    private void initPostProcessors() {
        var processorClasses = new Reflections(BASE_BRING_PACKAGE).getSubTypesOf(PostProcessor.class);
        for (Class<? extends PostProcessor> postProcessor : processorClasses) {
            try {
                var postProcessorInstance = postProcessor.getDeclaredConstructor().newInstance();
                postProcessors.add(postProcessorInstance);
                log.info(String.format("Registered %s post processor", postProcessor.getSimpleName()));
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new DefaultConstructorNotFoundException(format(DEFAULT_CONSTRUCTOR_NOT_FOUND_EXCEPTION, postProcessor.getSimpleName()));
            }
        }
    }

    private <T> T getPrimaryBean(Map<String, T> beanMapByType, Class<T> beanType) {
        Supplier<String> matchingBeanMessage = getMatchingBeanMessage(beanMapByType);

        Map<String, T> allBeansAnnotatedPrimary = beanMapByType.entrySet().stream()
                .filter(bean -> bean.getValue().getClass().isAnnotationPresent(Primary.class))
                .collect(toMap(Entry::getKey, Entry::getValue));

        if(allBeansAnnotatedPrimary.size() > 1) {
            throw new NoUniqueBeanException(format(NO_UNIQUE_PRIMARY_BEAN_EXCEPTION, allBeansAnnotatedPrimary.keySet()));
        }

        return allBeansAnnotatedPrimary.keySet().stream()
                .map(beanMapByType::get)
                .findFirst()
                .orElseThrow(() -> new NoUniqueBeanException(format(NO_UNIQUE_BEAN_EXCEPTION, beanType.getSimpleName(), matchingBeanMessage.get())));
    }

    private<T> Supplier<String> getMatchingBeanMessage(Map<String, T> beanMapByType) {
        return () -> beanMapByType.entrySet().stream()
                .map(bean -> bean.getKey() + ": " + bean.getValue().getClass().getSimpleName())
                .collect(joining(", "));
    }

    private void initProxyConfigurators() {
        var proxies = new Reflections(BASE_BRING_PACKAGE).getSubTypesOf(ProxyConfigurator.class);
        for (Class<? extends ProxyConfigurator> proxy : proxies) {
            try {
                var proxyInstance = proxy.getDeclaredConstructor().newInstance();
                proxyConfigurators.add(proxyInstance);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new DefaultConstructorNotFoundException(format(DEFAULT_CONSTRUCTOR_NOT_FOUND_EXCEPTION, proxy.getSimpleName()));
            }
        }
    }
}