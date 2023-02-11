package com.hoverla.bring.context.bean.definition;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.annotation.Configuration;
import com.hoverla.bring.annotation.Primary;
import com.hoverla.bring.context.bean.dependency.BeanDependency;
import com.hoverla.bring.context.util.ResolveDependenciesUtil;
import com.hoverla.bring.exception.BeanDependencyInjectionException;
import com.hoverla.bring.exception.BeanInstanceCreationException;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hoverla.bring.common.StringConstants.*;
import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * This class describes the internals of a {@link Bean} created by default approach
 * and provides a basic BeanDefinition implementation.
 * <p>
 * @see Configuration
 */
@Slf4j
public class DefaultBeanDefinition extends AbstractBeanDefinition {
    private Constructor<?> constructor;
    private List<Field> autowiredFields;

    public DefaultBeanDefinition(Class<?> beanClass) {
        Objects.requireNonNull(beanClass, BEAN_CLASS_ERROR_MESSAGE);

        this.type = beanClass;
        log.trace("'{}' bean type is '{}'", name, type);

        this.name = resolveName(beanClass);
        log.trace("Bean name is '{}'", name);

        this.dependencies = resolveDependencies(beanClass);
        log.trace("'{}' bean dependencies are {}", name, dependencies);
    }

    @Override
    public void instantiate(BeanDefinition... dependencies) {
        if (!isInstantiated()) {
            log.debug("Instantiating bean: '{}' of type {}", name, type.getName());
            List<BeanDefinition> dependencyList = new ArrayList<>(List.of(dependencies));
            Map<String, Class<?>> dependenciesMap = dependencyList.stream()
                .collect(toMap(BeanDefinition::name, BeanDefinition::type));
            log.trace("Bean '{}' of type {} has the following dependencies: {}", name, type.getName(), dependenciesMap);
            instance = createInstance(dependencyList);
            log.debug("Bean '{}' of type {} has been instantiated", type.getName(), name);
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
        Map<String, BeanDependency> allBeanDependencies = new HashMap<>();
        allBeanDependencies.putAll(resolveConstructorDependencies(beanClass));
        allBeanDependencies.putAll(resolveFieldDependencies(beanClass));

        return allBeanDependencies;
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

        List<String> paramNames = Stream.of(constructorParams).map(Parameter::getName).collect(Collectors.toList());
        log.debug("Constructor of class {} has the following parameters: {}",
            constructor.getDeclaringClass().getSimpleName(), paramNames);

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
        List<String> autowiredFieldNames = autowiredFields.stream().map(Field::getName).collect(Collectors.toList());
        log.debug("Class {} has the following autowired fields: {}", beanClass.getSimpleName(), autowiredFieldNames);

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
            Object beanInstance = createInstanceUsingConstructor(dependencies);
            if (!dependencies.isEmpty()) {
                doFieldAutowiring(beanInstance, dependencies);
            }
            this.instance = beanInstance;
            return instance;
        } catch (Exception e) {
            throw new BeanInstanceCreationException(String.format(BEAN_INSTANCE_CREATION_EXCEPTION, name), e);
        }
    }

    private Object doCreateInstance(List<BeanDefinition> dependencies) {
        Object beanInstance = createInstanceUsingConstructor(dependencies);

        if (!dependencies.isEmpty()) {
            doFieldAutowiring(beanInstance, dependencies);
        }
        return beanInstance;
    }

    private Object createInstanceUsingConstructor(List<BeanDefinition> dependencies) {
        if (constructor.getParameterCount() == 0) {
            try {
                return constructor.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(String.format(CAN_NOT_CREATE_INSTANCE, constructor.getName()), e);
            }
        }
        List<Parameter> parameters = new ArrayList<>(List.of(constructor.getParameters()));
        Object[] constructorArgs = new Object[parameters.size()];

        ResolveDependenciesUtil.resolveDependencies(dependencies, parameters, constructorArgs, name);
        try {
            return constructor.newInstance(constructorArgs);
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format(
                            CAN_NOT_CREATE_INSTANCE_WITH_ARGUMENTS,
                            constructor.getName(),
                            Arrays.toString(constructorArgs)), e);
        }
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
            log.warn("Could not autowire the following fields: {}", unresolvedFieldNames.size());
            throw new BeanDependencyInjectionException(String.format(
                    BEAN_DEPENDENCY_INJECTION_EXCEPTION,
                    beanInstance.getClass().getName(), unresolvedFieldNames)
            );
        }
    }

    private void setFieldValue(Field field, Object beanInstance, BeanDefinition dependency) {
        field.setAccessible(true);
        try {
            field.set(beanInstance, dependency.getInstance());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format(CAN_NOT_SET_FIELD, field, beanInstance), e);
        }
    }

    private boolean injectionFailedForField(Field targetField, Object beanInstance) {
        targetField.setAccessible(true);
        try {
            return targetField.get(beanInstance) == null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format(CAN_NOT_GET_FIELD, targetField, beanInstance), e);
        }
    }
}