package com.hoverla.bring.context;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.annotation.Primary;
import com.hoverla.bring.context.bean.definition.BeanDefinition;
import com.hoverla.bring.context.bean.definition.BeanDefinitionContainer;
import com.hoverla.bring.context.bean.initializer.BeanInitializer;
import com.hoverla.bring.context.bean.postprocessor.BeanPostProcessor;
import com.hoverla.bring.context.bean.scanner.BeanScanner;
import com.hoverla.bring.exception.DefaultConstructorNotFoundException;
import com.hoverla.bring.exception.NoSuchBeanException;
import com.hoverla.bring.exception.NoUniqueBeanException;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import static com.hoverla.bring.common.StringConstants.BASE_BRING_PACKAGE;
import static com.hoverla.bring.common.StringConstants.DEFAULT_CONSTRUCTOR_NOT_FOUND_EXCEPTION;
import static com.hoverla.bring.common.StringConstants.NO_SUCH_BEAN_EXCEPTION_BY_NAME_TYPE;
import static com.hoverla.bring.common.StringConstants.NO_SUCH_BEAN_EXCEPTION_BY_TYPE;
import static com.hoverla.bring.common.StringConstants.NO_UNIQUE_BEAN_EXCEPTION;
import static com.hoverla.bring.common.StringConstants.NO_UNIQUE_PRIMARY_BEAN_EXCEPTION;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Implementation of {@link ApplicationContext}
 * <p>
 * AnnotationApplicationContextImpl its base implementation of API allows working with Beans
 */
@Slf4j
public class DefaultApplicationContextImpl implements ApplicationContext {
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();
    private final BeanDefinitionContainer container;

    public DefaultApplicationContextImpl(List<BeanScanner> scanners, BeanInitializer initializer) {
        List<BeanDefinition> beanDefinitions = scanPackagesForBeanDefinitions(scanners);
        //TODO add validation for bean definitions
        container = new BeanDefinitionContainer(beanDefinitions);
        initializer.initialize(container);
        postProcess();
        log.info("Application context initialization has been finished");
    }

    /**
     * @see BeanDefinition
     */
    private List<BeanDefinition> scanPackagesForBeanDefinitions(List<BeanScanner> scanners) {
        return scanners.stream()
                .map(BeanScanner::scan)
                .flatMap(List::stream)
                .collect(toList());
    }

    /**
     * @return Bean instance by bean type
     * If a bean doesn't find will be thrown {@link NoSuchBeanException}
     */
    @Override
    public <T> T getBean(Class<T> beanType) {
        List<BeanDefinition> beanDefinitions = container.getBeansAssignableFromType(beanType);

        if (beanDefinitions.size() > 1) {
            return getPrimaryBean(beanDefinitions, beanType);
        }

        return beanDefinitions.stream().findFirst()
                .map(BeanDefinition::getInstance)
                .map(beanType::cast)
                .orElseThrow(() -> new NoSuchBeanException(
                        format(NO_SUCH_BEAN_EXCEPTION_BY_TYPE, beanType.getSimpleName())
                ));
    }

    /**
     * @return Bean instance by class name and bean type
     * If a bean doesn't find will be thrown {@link NoSuchBeanException}
     */
    @Override
    public <T> T getBean(String name, Class<T> beanType) {
        return container.getBeanDefinitionByName(name)
                .filter(potentialBean -> beanType.isAssignableFrom(potentialBean.getInstance().getClass()))
                .map(BeanDefinition::getInstance)
                .map(beanType::cast)
                .orElseThrow(() -> new NoSuchBeanException(
                        format(NO_SUCH_BEAN_EXCEPTION_BY_NAME_TYPE, name, beanType.getSimpleName())
                ));
    }

    /**
     * @return All available bean by type.
     */
    @Override
    public <T> Map<String, T> getAllBeans(Class<T> beanType) {
        return container.getBeansAssignableFromType(beanType)
                .stream()
                .collect(toMap(BeanDefinition::name, beanDefinition -> beanType.cast(beanDefinition.getInstance())));
    }

    /**
     * This method configures additional settings for beans and applies those settings to them.
     *
     * @see BeanPostProcessor
     * @see Bean
     */
    private void postProcess() {
        initPostProcessors();
        Collection<Object> beanInstances = container.getBeanDefinitions().stream()
                .map(BeanDefinition::getInstance).collect(toList());
        for (Object beanInstance : beanInstances) {
            beanPostProcessors.forEach(beanPostProcessor -> beanPostProcessor.process(beanInstance, this));
        }
    }

    /**
     * Method for registering additional configuration for beans.
     *
     * @see BeanPostProcessor
     * @see Bean
     */
    private void initPostProcessors() {
        var processorClasses = new Reflections(BASE_BRING_PACKAGE).getSubTypesOf(BeanPostProcessor.class);
        for (Class<? extends BeanPostProcessor> postProcessor : processorClasses) {
            try {
                beanPostProcessors.add(postProcessor.getDeclaredConstructor().newInstance());
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                throw new DefaultConstructorNotFoundException(
                        format(DEFAULT_CONSTRUCTOR_NOT_FOUND_EXCEPTION, postProcessor.getSimpleName()));
            }
        }
    }

    /**
     * @param beanDefinitions Current beans
     * @return If more than one bean is registered by type this method return the needed bean.
     * @see Primary
     */
    private <T> T getPrimaryBean(List<BeanDefinition> beanDefinitions, Class<T> beanType) {
        Supplier<String> matchingBeanMessage = getMatchingBeanMessage(beanDefinitions);

        Map<String, T> allBeansAnnotatedPrimary = beanDefinitions.stream()
                .filter(beanDefinition -> beanDefinition.type().isAnnotationPresent(Primary.class))
                .collect(toMap(BeanDefinition::name, beanDefinition -> beanType.cast(beanDefinition.getInstance())));

        if (allBeansAnnotatedPrimary.size() > 1) {
            throw new NoUniqueBeanException(format(NO_UNIQUE_PRIMARY_BEAN_EXCEPTION, allBeansAnnotatedPrimary.keySet()));
        }

        return allBeansAnnotatedPrimary.entrySet().stream().findFirst()
                .map(Entry::getValue)
                .orElseThrow(() -> new NoUniqueBeanException(
                        format(NO_UNIQUE_BEAN_EXCEPTION, beanType.getSimpleName(), matchingBeanMessage.get())));

    }

    private Supplier<String> getMatchingBeanMessage(List<BeanDefinition> beanDefinitions) {
        return () -> beanDefinitions.stream()
                .map(beanDefinition -> beanDefinition.name() + ": " + beanDefinition.type().getSimpleName())
                .collect(joining(", "));
    }
}