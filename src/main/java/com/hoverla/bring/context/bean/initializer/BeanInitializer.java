package com.hoverla.bring.context.bean.initializer;

import com.hoverla.bring.context.bean.definition.BeanDefinition;
import com.hoverla.bring.context.bean.definition.BeanDefinitionContainer;
import com.hoverla.bring.context.bean.dependency.BeanDependency;
import com.hoverla.bring.context.bean.dependency.BeanDependencyNameResolver;
import com.hoverla.bring.exception.BeanInitializePhaseException;
import com.hoverla.bring.exception.NoSuchBeanException;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

import static com.hoverla.bring.exception.NoSuchBeanException.NO_SUCH_BEAN_EXCEPTION_BY_NAME_TYPE;
import static java.lang.String.format;

@RequiredArgsConstructor
public class BeanInitializer {
    private final BeanDependencyNameResolver dependencyNameResolver;

    public void initialize(BeanDefinitionContainer container) {
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

        BeanDefinition[] beanDependencies = getBeanDependencies(definitionToInitialize, container);
        if (beanDependencies.length == 0) {
            definitionToInitialize.instantiate();
            return;
        }

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
