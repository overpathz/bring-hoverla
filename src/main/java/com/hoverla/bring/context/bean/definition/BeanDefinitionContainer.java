package com.hoverla.bring.context.bean.definition;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toConcurrentMap;
import static java.util.stream.Collectors.toList;

public class BeanDefinitionContainer {
    private final Map<String, BeanDefinition> beanDefinitions;

    public BeanDefinitionContainer(List<BeanDefinition> beanDefinitions) {
        this.beanDefinitions = beanDefinitions
            .stream()
            .collect(toConcurrentMap(BeanDefinition::name, Function.identity()));
    }

    public Optional<BeanDefinition> getBeanDefinitionByName(String name) {
        return Optional.ofNullable(beanDefinitions.get(name));
    }

    public List<BeanDefinition> getBeansAssignableFromType(Class<?> type) {
        return beanDefinitions.values()
            .stream()
            .filter(bd -> type.isAssignableFrom(bd.type()))
            .collect(toList());
    }

    public List<BeanDefinition> getBeansWithExactType(Class<?> type) {
        return beanDefinitions.values()
            .stream()
            .filter(bd -> type.equals(bd.type()))
            .collect(toList());
    }

    public Collection<BeanDefinition> getBeanDefinitions() {
        return beanDefinitions.values();
    }

}
