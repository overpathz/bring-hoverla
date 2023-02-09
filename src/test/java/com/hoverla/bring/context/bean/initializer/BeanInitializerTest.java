package com.hoverla.bring.context.bean.initializer;

import com.hoverla.bring.context.bean.definition.BeanDefinition;
import com.hoverla.bring.context.bean.definition.BeanDefinitionContainer;
import com.hoverla.bring.context.bean.dependency.BeanDependency;
import com.hoverla.bring.context.bean.dependency.BeanDependencyNameResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BeanInitializerTest {
    private static final String BEAN_DEFINITION_1 = "beanDefinition1";
    private static final String BEAN_DEFINITION_2 = "beanDefinition2";
    private static final String BEAN_DEFINITION_3 = "beanDefinition3";
    private static final String BEAN_DEFINITION_4 = "beanDefinition4";
    private static final String BEAN_DEFINITION_5 = "beanDefinition5";

    private BeanInitializer beanInitializer;
    private BeanDependencyNameResolver nameResolver;

    @BeforeEach
    void setUp() {
        nameResolver = mock(BeanDependencyNameResolver.class);
        beanInitializer = new BeanInitializer(nameResolver);
    }

    @Test
    @DisplayName("Initialization test. Verifies that 'instance' method of BeanDefinition is called with correct dependencies")
    void testInitialize() {
        BeanDefinition beanDefinition1 = getDefinition(BEAN_DEFINITION_1, BEAN_DEFINITION_2, BEAN_DEFINITION_3);
        BeanDefinition beanDefinition2 = getDefinition(BEAN_DEFINITION_2, BEAN_DEFINITION_3, BEAN_DEFINITION_4);
        BeanDefinition beanDefinition3 = getDefinition(BEAN_DEFINITION_3, BEAN_DEFINITION_4);
        BeanDefinition beanDefinition4 = getDefinition(BEAN_DEFINITION_4);
        BeanDefinition beanDefinition5 = getDefinition(BEAN_DEFINITION_5);


        List<BeanDefinition> beans = List.of(beanDefinition1, beanDefinition2, beanDefinition3, beanDefinition4);
        BeanDefinitionContainer container = new BeanDefinitionContainer(beans);

        Map<BeanDefinition, BeanDefinition[]> expectedInvocationArgs = Map.of(
            beanDefinition1, new BeanDefinition[]{beanDefinition2, beanDefinition3},
            beanDefinition2, new BeanDefinition[]{beanDefinition3, beanDefinition4},
            beanDefinition3, new BeanDefinition[]{beanDefinition4},
            beanDefinition4, new BeanDefinition[0],
            beanDefinition5, new BeanDefinition[0]
        );

        beanInitializer.initialize(container);

        verify(nameResolver).resolveDependencyNames(container);

        for (var beanDefinition : container.getBeanDefinitions()) {
            ArgumentCaptor<BeanDefinition> beanDefinitionArgumentCaptor = ArgumentCaptor.forClass(BeanDefinition.class);

            verify(beanDefinition, times(1)).instantiate(beanDefinitionArgumentCaptor.capture());

            BeanDefinition[] expectedDependencies = expectedInvocationArgs.get(beanDefinition);
            List<BeanDefinition> actualDependencies = beanDefinitionArgumentCaptor.getAllValues();

            assertThat(actualDependencies).hasSize(expectedDependencies.length);
            assertThat(actualDependencies).containsExactlyInAnyOrder(expectedDependencies);
        }
    }

    private BeanDefinition getDefinition(String beanDefinitionName, String... dependencyNames) {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        doReturn(BeanDefinition.class).when(beanDefinition).type();
        when(beanDefinition.name()).thenReturn(beanDefinitionName);

        Map<String, BeanDependency> dependencies = new HashMap<>();
        for (var name : dependencyNames) {
            dependencies.put(name, new BeanDependency(name, BeanDefinition.class));
        }
        when(beanDefinition.dependencies()).thenReturn(dependencies);
        Supplier<?> instantiateAnswerSupplier = () -> when(beanDefinition.isInstantiated()).thenReturn(true);
        doAnswer(ignore -> instantiateAnswerSupplier.get()).when(beanDefinition).instantiate(any());
        return beanDefinition;
    }
}
