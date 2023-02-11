package com.hoverla.bring.context.bean.definition;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.context.bean.dependency.BeanDependency;
import com.hoverla.bring.context.fixtures.config.TestConfiguration;
import com.hoverla.bring.exception.BeanDependencyInjectionException;
import com.hoverla.bring.exception.BeanInstanceCreationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigurationBeanDefinitionTest {

    private TestConfiguration testConfiguration;

    @BeforeEach
    void setUp() {
        testConfiguration = new TestConfiguration();
    }

    @Test
    @DisplayName("Bean name is taken from method name if it's not specified via annotation")
    void unnamedBean() throws NoSuchMethodException {
        String methodName = "unnamedBean";
        Method method = testConfiguration.getClass().getMethod(methodName);

        BeanDefinition beanDefinition = new ConfigurationBeanDefinition(testConfiguration, method);

        assertNotNull(beanDefinition);
        assertEquals(methodName, beanDefinition.name());
    }

    @Test
    @DisplayName("Bean name is taken from @Bean annotation value")
    void beanWithNameInAnnotation() throws NoSuchMethodException {
        Method method = testConfiguration.getClass().getMethod("namedBean");
        String beanName = method.getAnnotation(Bean.class).value();
        Class<?> returnType = method.getReturnType();

        BeanDefinition beanDefinition = new ConfigurationBeanDefinition(testConfiguration, method);

        assertNotNull(beanDefinition);
        assertEquals(beanName, beanDefinition.name());
        assertEquals(returnType, beanDefinition.type());
    }


    @Test
    @DisplayName("Bean dependencies match their parameter name")
    void beanWithDependencies() throws NoSuchMethodException {
        String methodName = "beanWithDependencies";
        Method method = testConfiguration.getClass().getMethod(methodName, long.class, String.class);

        BeanDefinition firstDependency = mock(BeanDefinition.class);
        when(firstDependency.getInstance()).thenReturn(101L);
        BeanDefinition secondDependency = mock(BeanDefinition.class);
        when(secondDependency.getInstance()).thenReturn("23");

        BeanDependency longDependency = new BeanDependency(long.class.getName(), Long.class);
        BeanDependency stringDependency = new BeanDependency(String.class.getName(), String.class);
        var dependencies = Map.of(longDependency.getName(), longDependency, stringDependency.getName(), stringDependency);

        BeanDefinition beanDefinition = new ConfigurationBeanDefinition(testConfiguration, method);

        assertNotNull(beanDefinition);
        assertEquals(dependencies, beanDefinition.dependencies());
    }

    @Test
    @DisplayName("Missing dependencies cause BeanInstanceCreationException")
    void beanWithMissingDependencies() throws NoSuchMethodException {
        String methodName = "beanWithDependencies";
        Method method = testConfiguration.getClass().getMethod(methodName, long.class, String.class);

        BeanDefinition beanDefinition = new ConfigurationBeanDefinition(testConfiguration, method);

        Assertions.assertThatThrownBy(beanDefinition::instantiate)
            .isInstanceOf(BeanInstanceCreationException.class)
            .hasMessageContaining("Bean with name 'beanWithDependencies' can't be instantiated")
            .hasRootCauseInstanceOf(BeanDependencyInjectionException.class)
            .hasStackTraceContaining("bean has no dependency that matches parameter");
    }

    @Test
    @DisplayName("Beans are instantiated only on instantiate method call")
    void beansAreInstantiatedLazily() throws NoSuchMethodException {
        Method method = testConfiguration.getClass().getMethod("namedBean");

        var beanDefinition = new ConfigurationBeanDefinition(testConfiguration, method);
        assertNull(beanDefinition.getInstance());

        beanDefinition.instantiate();
        assertNotNull(beanDefinition.getInstance());
    }

    @Test
    @DisplayName("Bean constructor dependencies are kept in order")
    void beanConstructorWithCorrectArgumentsOrder() throws NoSuchMethodException {
        Method method = testConfiguration.getClass().getMethod("beanWithDependencies", long.class,
            String.class);

        long n = 101L;
        BeanDefinition firstDependency = mock(BeanDefinition.class);
        when(firstDependency.getInstance()).thenReturn(n);
        when(firstDependency.name()).thenReturn("n");
        doReturn(long.class).when(firstDependency).type();

        String s = "23";
        BeanDefinition secondDependency = mock(BeanDefinition.class);
        when(secondDependency.getInstance()).thenReturn(s);
        when(secondDependency.name()).thenReturn("s");
        doReturn(String.class).when(secondDependency).type();

        var beanDefinition = new ConfigurationBeanDefinition(testConfiguration, method);

        assertThatNoException()
            .isThrownBy(() -> beanDefinition.instantiate(secondDependency, firstDependency));

        Object instance = beanDefinition.getInstance();

        assertEquals(n - Long.parseLong(s), instance);
    }
}
