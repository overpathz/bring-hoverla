package com.hoverla.bring.context.bean.dependency;

import com.hoverla.bring.context.bean.definition.BeanDefinition;
import com.hoverla.bring.context.bean.definition.BeanDefinitionContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BeanDependencyNameResolverTest {
    private BeanDependencyNameResolver nameResolver;

    @BeforeEach
    void setUp() {
        nameResolver = new BeanDependencyNameResolver();
    }

    @Test
    @DisplayName("Replace bean name with a custom name from bean definition")
    void replaceDefaultNameToCustomTest() {
        var beanName = "date";
        BeanDefinition dependencyDefinition = getDefinition(beanName, LocalDate.class, emptyMap());

        BeanDependency dependency = new BeanDependency(LocalDate.class.getName(), LocalDate.class);
        Map<String, BeanDependency> dependencies = new HashMap<>(Map.of(LocalDate.class.getName(), dependency));
        BeanDefinition dependentDefinition = getDefinition("beanWithDependencies", Object.class, dependencies);

        List<BeanDefinition> beans = List.of(dependencyDefinition, dependentDefinition);
        BeanDefinitionContainer container = new BeanDefinitionContainer(beans);

        nameResolver.resolveDependencyNames(container);

        Map<String, BeanDependency> definitionDependencies = dependentDefinition.dependencies();
        assertTrue(definitionDependencies.containsKey(beanName));
        assertEquals(dependency, definitionDependencies.get(beanName));
    }

    @Test
    @DisplayName("Replaces default dependency names with primary bean custom when there are more than 1 bean of the same type")
    void replacesDefaultNameToPrimaryBeanName() {
        BeanDefinition nonPrimaryDependency = getDefinition("string1", String.class, emptyMap());
        BeanDefinition primaryDependency = getDefinition("string2", String.class, emptyMap());
        when(primaryDependency.isPrimary()).thenReturn(true);

        BeanDependency dependency = new BeanDependency(String.class.getName(), String.class);
        Map<String, BeanDependency> dependencies = new HashMap<>(Map.of(dependency.getName(), dependency));
        BeanDefinition dependentDefinition = getDefinition("bean", Object.class, dependencies);

        List<BeanDefinition> beans = List.of(nonPrimaryDependency, primaryDependency, dependentDefinition);
        BeanDefinitionContainer container = new BeanDefinitionContainer(beans);

        nameResolver.resolveDependencyNames(container);

        Map<String, BeanDependency> definitionDependencies = dependentDefinition.dependencies();
        assertTrue(definitionDependencies.containsKey(primaryDependency.name()));
        assertFalse(definitionDependencies.containsKey(nonPrimaryDependency.name()));

        assertThat(dependentDefinition.dependencies())
            .containsKey(primaryDependency.name())
            .doesNotContainKeys(Integer.class.getName(), nonPrimaryDependency.name());
    }

    private BeanDefinition getDefinition(String beanName, Class<?> type, Map<String, BeanDependency> dependencies) {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        doReturn(type).when(beanDefinition).type();
        when(beanDefinition.name()).thenReturn(beanName);
        when(beanDefinition.dependencies()).thenReturn(dependencies);
        return beanDefinition;
    }
}
