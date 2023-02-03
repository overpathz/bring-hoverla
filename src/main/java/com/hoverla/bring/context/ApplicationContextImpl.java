package com.hoverla.bring.context;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.exception.ApplicationContextInitializationException;
import com.hoverla.bring.exception.NoSuchBeanException;
import com.hoverla.bring.exception.NoUniqueBeanException;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ApplicationContextImpl implements ApplicationContext {
    private final Map<String, Object> beans = new ConcurrentHashMap<>();

    public ApplicationContextImpl(String... basePackages) {
        Reflections beanScanner = new Reflections((Object[]) basePackages);
        Set<Class<?>> beanClasses = beanScanner.getTypesAnnotatedWith(Bean.class, true);

        if (beanClasses.isEmpty()) {
            return;
        }

        try {
            initBeans(beanClasses);
            postProcess();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                 | IllegalAccessException e) {
            throw new ApplicationContextInitializationException(
                format("ApplicationContext initialization has failed: %s", e.getMessage())
            );
        }
    }

    private void initBeans(Set<Class<?>> beanClasses) throws NoSuchMethodException, InvocationTargetException,
        InstantiationException, IllegalAccessException {
        for (Class<?> beanType: beanClasses) {
            String beanName = resolveBeanName(beanType);
            Object instance = beanType.getConstructor().newInstance();
            beans.put(beanName, instance);
        }
    }

    @SuppressWarnings("java:S3011")
    private void postProcess() throws IllegalAccessException {
        Collection<Object> beanInstances = beans.values();

        for (Object beanInstance: beanInstances) {
            for (Field field: beanInstance.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    field.set(beanInstance, getBean(field.getType()));
                }
            }
        }

    }

    private String resolveBeanName(Class<?> type) {
        String beanName = type.getAnnotation(Bean.class).value();
        return isNotBlank(beanName) ? beanName : resolveBeanName(type.getSimpleName());
    }

    private String resolveBeanName(String typeName) {
        return typeName.substring(0, 1).toLowerCase() + typeName.substring(1);
    }

    @Override
    public <T> T getBean(Class<T> beanType) {
        Map<String, T> beanMap = getAllBeans(beanType);

        if (beanMap.size() > 1) {
            String matchingBeans = beanMap.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue().getClass().getSimpleName())
                .collect(joining(", "));
            throw new NoUniqueBeanException(format("There is more than one bean matching the %s type: [%s]. " +
                "Please specify a bean name!", beanType.getSimpleName(), matchingBeans));
        }


        return beanMap.entrySet().stream().findFirst()
            .map(Entry::getValue)
            .orElseThrow(() -> new NoSuchBeanException(
                format("Bean with type %s not found", beanType.getSimpleName())
            ));
    }

    @Override
    public <T> T getBean(String name, Class<T> beanType) {
        return Optional.ofNullable(beans.get(name))
            .filter(potentialBean -> beanType.isAssignableFrom(potentialBean.getClass()))
            .map(beanType::cast)
            .orElseThrow(() -> new NoSuchBeanException(
                format("Bean with name %s and type %s not found", name, beanType.getSimpleName())
            ));
    }

    @Override
    public <T> Map<String, T> getAllBeans(Class<T> beanType) {
        return beans.entrySet().stream()
            .filter(potentialBean -> beanType.isAssignableFrom(potentialBean.getValue().getClass()))
            .collect(toMap(Entry::getKey, bean -> beanType.cast(bean.getValue())));
    }
}
