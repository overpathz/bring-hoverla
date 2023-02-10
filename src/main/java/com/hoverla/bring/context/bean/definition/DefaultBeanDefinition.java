package com.hoverla.bring.context.bean.definition;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.annotation.Primary;
import com.hoverla.bring.context.bean.dependency.BeanDependency;
import com.hoverla.bring.context.util.ResolveDependenciesUtil;
import com.hoverla.bring.exception.BeanDependencyInjectionException;
import com.hoverla.bring.exception.BeanInstanceCreationException;
import lombok.SneakyThrows;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class DefaultBeanDefinition extends AbstractBeanDefinition {
    private Constructor<?> constructor;
    private List<Field> autowiredFields;

    public DefaultBeanDefinition(Class<?> beanClass) {
        Objects.requireNonNull(beanClass, "Bean class cannot be null");
        this.type = beanClass;
        this.name = resolveName(beanClass);
        this.dependencies = resolveDependencies(beanClass);
    }

    @Override
    public void instantiate(BeanDefinition... dependencies) {
        if (!isInstantiated()) {
            List<BeanDefinition> dependencyList = new ArrayList<>(List.of(dependencies));
            instance = createInstance(dependencyList);
        }
    }

    @Override
    public boolean isPrimary() {
        return type.isAnnotationPresent(Primary.class);
    }

    private String resolveName(Class<?> beanClass) {
        Bean beanAnnotation = beanClass.getAnnotation(Bean.class);
        String beanName = beanAnnotation.value();
        if (isBlank(beanName)) {
            return beanClass.getName();
        }
        return beanName;
    }

    private Map<String, BeanDependency> resolveDependencies(Class<?> beanClass) {
        Map<String, BeanDependency> allDependencies = new HashMap<>();
        allDependencies.putAll(resolveConstructorDependencies(beanClass));
        allDependencies.putAll(resolveFieldDependencies(beanClass));

        return allDependencies;
    }

    private Map<String, BeanDependency> resolveConstructorDependencies(Class<?> beanClass) {
        Constructor<?>[] beanConstructors = beanClass.getConstructors();

        return Stream.of(beanConstructors)
            .filter(injectedConstructor -> injectedConstructor.isAnnotationPresent(Autowired.class))
            .findFirst()
            .map(this::resolveConstructorDependencies)
            .orElseGet(() -> resolveConstructorDependencies(beanConstructors[0]));
    }

    private Map<String, BeanDependency> resolveConstructorDependencies(Constructor<?> beanConstructor) {
        this.constructor = beanConstructor;

        Parameter[] constructorParams = constructor.getParameters();
        if (isEmpty(constructorParams)) {
            return emptyMap();
        }
        return Stream.of(constructorParams)
            .map(BeanDependency::fromParameter)
            .collect(toMap(BeanDependency::getName, identity()));
    }

    private Map<String, BeanDependency> resolveFieldDependencies(Class<?> beanClass) {
        List<Field> classFields = new ArrayList<>(List.of(beanClass.getDeclaredFields()));
        this.autowiredFields = getAutowiredElements(classFields);

        if (autowiredFields.isEmpty()) {
            return emptyMap();
        }
        return autowiredFields.stream()
            .map(BeanDependency::fromField)
            .collect(toMap(BeanDependency::getName, identity()));
    }

    private <T extends AnnotatedElement> List<T> getAutowiredElements(List<T> elements) {
        return elements.stream()
            .filter(e -> e.isAnnotationPresent(Autowired.class))
            .collect(Collectors.toList());
    }

    private Object createInstance(List<BeanDefinition> dependencies) {
        try {
            Objects.requireNonNull(dependencies);
            this.instance = doCreateInstance(dependencies);
            return instance;
        } catch (Exception e) {
            throw new BeanInstanceCreationException(String.format("Bean with name '%s' can't be instantiated", name), e);
        }
    }

    @SneakyThrows
    private Object doCreateInstance(List<BeanDefinition> dependencies) {
        Object beanInstance = createInstanceUsingConstructor(dependencies);

        if (!dependencies.isEmpty()) {
            doFieldAutowiring(beanInstance, dependencies);
        }
        return beanInstance;
    }

    @SneakyThrows
    private Object createInstanceUsingConstructor(List<BeanDefinition> dependencies) {
        if (constructor.getParameterCount() == 0) {
            return constructor.newInstance();
        }
        List<Parameter> parameters = new ArrayList<>(List.of(constructor.getParameters()));
        Object[] constructorArgs = new Object[parameters.size()];

        ResolveDependenciesUtil.resolveDependencies(dependencies, parameters, constructorArgs, name);
        return constructor.newInstance(constructorArgs);
    }

    private void doFieldAutowiring(Object beanInstance, List<BeanDefinition> dependencies) {
        autowiredFields
            .stream()
            .sorted(this::subclassesFirst)
            .forEach(field -> autowireField(beanInstance, field, dependencies));

        verifyFieldAutowiring(beanInstance);
    }

    private int subclassesFirst(Field field,
                                       Field anotherField) {
        Class<?> fieldType = field.getType();
        Class<?> anotherFieldType = anotherField.getType();
        if (fieldType.isAssignableFrom(anotherFieldType)) {
            return 1;
        }
        return -1;
    }

    private void autowireField(Object beanInstance, Field targetField, List<BeanDefinition> dependencies) {
        dependencies.stream()
            .filter(dependency -> fieldMatch(targetField, dependency))
            .findAny()
            .ifPresent(dependency -> {
                setFieldValue(targetField, beanInstance, dependency);
                dependencies.remove(dependency);
            });
    }

    @SuppressWarnings("java:S2589")
    private boolean fieldMatch(Field field, BeanDefinition dependencyToMatch) {
        Class<?> fieldType = field.getType();
        return fieldType.isAssignableFrom(dependencyToMatch.type());
    }

    private void verifyFieldAutowiring(Object beanInstance) {
        List<String> unresolvedFieldNames = autowiredFields.stream()
            .filter(field -> injectionFailedForField(field, beanInstance))
            .map(Field::getName)
            .collect(Collectors.toList());
        if (!unresolvedFieldNames.isEmpty()) {
            throw new BeanDependencyInjectionException(String.format(
                "Field injection failed for bean instance of type %s. Unresolved fields: %s",
                beanInstance.getClass().getName(), unresolvedFieldNames)
            );
        }
    }

    @SneakyThrows
    @SuppressWarnings("java:S3011")
    private void setFieldValue(Field field, Object beanInstance, BeanDefinition dependency) {
        field.setAccessible(true);
        field.set(beanInstance, dependency.getInstance());
    }

    @SneakyThrows
    @SuppressWarnings("java:S3011")
    private boolean injectionFailedForField(Field targetField, Object beanInstance) {
        targetField.setAccessible(true);
        return targetField.get(beanInstance) == null;
    }
}
