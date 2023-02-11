package com.hoverla.bring.context.bean.initializer;

import com.hoverla.bring.context.bean.definition.BeanDefinition;
import com.hoverla.bring.context.bean.definition.BeanDefinitionContainer;
import com.hoverla.bring.context.bean.dependency.BeanDependency;
import com.hoverla.bring.context.bean.dependency.BeanDependencyNameResolver;
import com.hoverla.bring.exception.BeanInitializePhaseException;
import com.hoverla.bring.exception.NoSuchBeanException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import static com.hoverla.bring.exception.NoSuchBeanException.NO_SUCH_BEAN_EXCEPTION_BY_NAME_TYPE;
import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;

@Slf4j
@RequiredArgsConstructor
public class BeanInitializer {
    private final BeanDependencyNameResolver dependencyNameResolver;

    public void initialize(BeanDefinitionContainer container) {
        log.debug("Bean initialization has been started");

        dependencyNameResolver.resolveDependencyNames(container);

        Collection<BeanDefinition> beanDefinitions = container.getBeanDefinitions();
        try {
            beanDefinitions.forEach(beanDefinition -> doInitialize(beanDefinition, container));
        } catch (Exception ex) {
            throw new BeanInitializePhaseException("Can't initialize beans", ex);
        }
    }

    private void doInitialize(BeanDefinition definitionToInitialize, BeanDefinitionContainer container) {
        if (definitionToInitialize.isInstantiated()) {
            return;
        }
        String beanName = definitionToInitialize.name();
        String typeName = definitionToInitialize.type().getName();
        log.trace("Initializing the bean definition with name '{}' and type {}", beanName, typeName);

        BeanDefinition[] beanDependencies = getBeanDependencies(definitionToInitialize, container);
        int dependenciesCount = beanDependencies.length;
        if (dependenciesCount == 0) {
            definitionToInitialize.instantiate();
            return;
        }
        Map<String, Class<?>> dependenciesMap = Stream.of(beanDependencies)
            .collect(toMap(BeanDefinition::name, BeanDefinition::type));
        log.debug("Bean '{}' of type {} has the following dependencies: {}", beanName, typeName, dependenciesMap);

        for (BeanDefinition beanDependency : beanDependencies) {
            if (!beanDependency.isInstantiated()) {
                doInitialize(beanDependency, container);
            }
        }
        definitionToInitialize.instantiate(beanDependencies);
    }

    private BeanDefinition[] getBeanDependencies(BeanDefinition rootDefinition, BeanDefinitionContainer container) {
        return rootDefinition.dependencies()
            .values()
            .stream()
            .map(dependency -> getDependency(dependency, rootDefinition, container))
            .toArray(BeanDefinition[]::new);
    }

    private BeanDefinition getDependency(BeanDependency dependency, BeanDefinition rootDefinition,
                                         BeanDefinitionContainer container) {
        return container
            .getBeanDefinitionByName(dependency.getName())
            .orElseThrow(() -> new NoSuchBeanException(format(NO_SUCH_BEAN_EXCEPTION_BY_NAME_TYPE,
                dependency.getName(), rootDefinition.name())));
    }
}
