package com.hoverla.bring.context.bean.definition;

import java.time.LocalDate;
import java.time.Month;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.context.bean.dependency.BeanDependency;
import com.hoverla.bring.context.fixtures.autowired.definition.success.BeanWithAutowiredConstructor;
import com.hoverla.bring.context.fixtures.autowired.definition.success.BeanWithCombinedAutowired;
import com.hoverla.bring.context.fixtures.autowired.definition.success.BeanWithConstructor;
import com.hoverla.bring.context.fixtures.autowired.definition.success.BeanWithMultipleAutowiredFields;
import com.hoverla.bring.context.fixtures.autowired.definition.success.BeanWithSetterAutowiring;
import com.hoverla.bring.context.fixtures.bean.success.TestBeanWithoutName;
import com.hoverla.bring.exception.BeanDependencyInjectionException;
import com.hoverla.bring.exception.BeanInstanceCreationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DefaultBeanDefinitionTest {

    @ParameterizedTest(name = "[{index}] - Resolve bean dependencies - {2}")
    @MethodSource("resolveDependencies")
    void resolveBeanDependenciesTest(Class<?> beanClass, Map<String, BeanDependency> expectedDependencies, String description) {
        var beanDefinition = new DefaultBeanDefinition(beanClass);
        assertEquals(expectedDependencies, beanDefinition.dependencies());
    }

    @ParameterizedTest(name = "[{index}] - Resolve bean name - {2}")
    @MethodSource("resolveBeanNames")
    void resolveBeanNameTest(Class<?> beanClass, String expectedName, String description) {
        var beanDefinition = new DefaultBeanDefinition(beanClass);

        assertEquals(expectedName, beanDefinition.name());
    }

    @Test
    @DisplayName("Successful instantiation of bean without dependencies")
    void createBeanWithoutDependencies() {
        Class<?> beanClass = TestBeanWithoutName.class;
        var beanDefinition = new DefaultBeanDefinition(beanClass);

        beanDefinition.instantiate();
        Object instance = beanDefinition.getInstance();

        assertNotNull(instance);
        assertEquals(beanClass, beanDefinition.type());
    }

    @Test
    @DisplayName("Successful instantiation of a bean with @Autowired constructor")
    void createBeanWithAutowiredConstructor() {
        Class<?> beanClass = BeanWithAutowiredConstructor.class;
        var beanDefinition = new DefaultBeanDefinition(beanClass);

        var fieldOne = "String";
        var fieldTwo = LocalDate.of(2023, Month.FEBRUARY, 8);
        BeanDefinition dependencyOne = getDefinition(String.class, String.class.getName(), fieldOne);
        BeanDefinition dependencyTwo = getDefinition(LocalDate.class, LocalDate.class.getName(), fieldTwo);

        beanDefinition.instantiate(dependencyOne, dependencyTwo);
        Object instance = beanDefinition.getInstance();

        assertNotNull(instance);
        BeanWithAutowiredConstructor beanInstance = (BeanWithAutowiredConstructor) instance;
        assertEquals(fieldOne, beanInstance.getString());
        assertEquals(fieldTwo, beanInstance.getDate());
    }

    @Test
    @DisplayName("Successful instantiation of a bean with default constructor")
    void createBeanWithDefaultConstructor() {
        Class<?> beanClass = BeanWithConstructor.class;
        var beanDefinition = new DefaultBeanDefinition(beanClass);

        var fieldOne = "String";
        var fieldTwo = LocalDate.of(2023, Month.FEBRUARY, 8);
        BeanDefinition dependencyOne = getDefinition(String.class, String.class.getName(), fieldOne);
        BeanDefinition dependencyTwo = getDefinition(LocalDate.class, LocalDate.class.getName(), fieldTwo);

        beanDefinition.instantiate(dependencyOne, dependencyTwo);
        Object instance = beanDefinition.getInstance();

        assertNotNull(instance);
        BeanWithConstructor beanInstance = (BeanWithConstructor) instance;
        assertEquals(fieldOne, beanInstance.getString());
        assertEquals(fieldTwo, beanInstance.getDate());
    }

    @Test
    @DisplayName("Unsuccessful instantiation of bean with 2-argument default constructor. Constructor parameters do not match")
    void createBeanWithNotMatchingConstructorDependencies() {
        Class<?> beanClass = BeanWithConstructor.class;
        var beanDefinition = new DefaultBeanDefinition(beanClass);

        var fieldOne = "String";
        var fieldTwo = "NextString";
        BeanDefinition dependencyOne = getDefinition(String.class, "gg", fieldOne);
        BeanDefinition dependencyTwo = getDefinition(String.class, "wp", fieldTwo);

        BeanInstanceCreationException exception = assertThrows(BeanInstanceCreationException.class,
            () -> beanDefinition.instantiate(dependencyOne, dependencyTwo));

        assertEquals("Bean with name 'someBean' can't be instantiated",
            exception.getMessage());
        assertEquals(BeanDependencyInjectionException.class, exception.getCause().getClass());
    }

    @Test
    @DisplayName("Successful instantiation of bean with 3 @Autowired fields")
    void createBeanWithAutowiredFields() {
        Class<?> beanClass = BeanWithMultipleAutowiredFields.class;
        var beanDefinition = new DefaultBeanDefinition(beanClass);

        var fieldOne = new TestBeanWithoutName();
        var fieldTwo = "23";
        var fieldThree = 1.2D;
        BeanDefinition dependencyOne = getDefinition(TestBeanWithoutName.class, TestBeanWithoutName.class.getName(),
            fieldOne);
        BeanDefinition dependencyTwo = getDefinition(String.class, String.class.getName(), fieldTwo);
        BeanDefinition dependencyThree = getDefinition(Double.class, Double.class.getName(), fieldThree);

        beanDefinition.instantiate(dependencyOne, dependencyTwo, dependencyThree);
        Object instance = beanDefinition.getInstance();

        assertNotNull(instance);
        BeanWithMultipleAutowiredFields beanInstance = (BeanWithMultipleAutowiredFields) instance;
        assertEquals(fieldOne, beanInstance.getTestBeanWithoutName());
        assertEquals(fieldTwo, beanInstance.getString());
        assertEquals(fieldThree, beanInstance.getADouble());
    }

    @Test
    @DisplayName("Unsuccessful instantiation of bean with 3 @Autowired fields. Only 2 field matched and autowired.")
    void createBeanWithNotMatchingFieldDependencies() {
        Class<?> beanClass = BeanWithMultipleAutowiredFields.class;
        var beanDefinition = new DefaultBeanDefinition(beanClass);

        BeanDefinition allGood = getDefinition(TestBeanWithoutName.class, "gg", new TestBeanWithoutName());
        BeanDefinition wrongNameAndType = getDefinition(Integer.class, "name", 23);
        BeanDefinition allGoodTwo = getDefinition(Double.class, Double.class.getName(), 1.2D);

        BeanInstanceCreationException exception = assertThrows(BeanInstanceCreationException.class,
            () -> beanDefinition.instantiate(allGood, wrongNameAndType, allGoodTwo));

        assertEquals("Bean with name " +
            "'com.hoverla.bring.context.fixtures.autowired.definition.success.BeanWithMultipleAutowiredFields' " +
            "can't be instantiated", exception.getMessage());
        assertEquals(BeanDependencyInjectionException.class, exception.getCause().getClass());
        assertEquals("Field injection failed for bean instance of type " +
            "com.hoverla.bring.context.fixtures.autowired.definition.success.BeanWithMultipleAutowiredFields. " +
            "Unresolved fields: [string]", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("Successful instantiation of bean with both @Autowired constructor and @Autowired field")
    void createBeanWithBothAutowiredConstructorAndFields() {
        Class<?> beanClass = BeanWithCombinedAutowired.class;
        var beanDefinition = new DefaultBeanDefinition(beanClass);

        var fieldOne = "str";
        var fieldTwo = LocalDate.of(2023, Month.FEBRUARY, 9);
        BeanDefinition dependencyOne = getDefinition(BeanWithAutowiredConstructor.class, BeanWithAutowiredConstructor.class.getName(),
            new BeanWithAutowiredConstructor(fieldOne, fieldTwo));
        BeanDefinition dependencyTwo = getDefinition(BeanWithConstructor.class, BeanWithConstructor.class.getName(),
            new BeanWithConstructor(fieldOne, fieldTwo));

        beanDefinition.instantiate(dependencyOne, dependencyTwo);
        Object instance = beanDefinition.getInstance();

        assertNotNull(instance);

        BeanWithCombinedAutowired bean = (BeanWithCombinedAutowired) instance;
        assertEquals(fieldOne, bean.getBeanWithConstructor().getString());
        assertEquals(fieldTwo, bean.getBeanWithConstructor().getDate());

        assertEquals(fieldOne, bean.getBeanWithAutowiredConstructor().getString());
        assertEquals(fieldTwo, bean.getBeanWithAutowiredConstructor().getDate());


    }

    private Stream<Arguments> resolveDependencies() {
        return Stream.of(
            Arguments.of(BeanWithConstructor.class,
                Map.ofEntries(
                    getDependency(String.class),
                    getDependency(LocalDate.class)
                ),
                "Default constructor dependencies are resolved"
            ),
            Arguments.of(BeanWithAutowiredConstructor.class,
                Map.ofEntries(
                    getDependency(String.class),
                    getDependency(LocalDate.class)
                ),
                "@Autowired constructor dependencies are resolved"
            ),
            Arguments.of(BeanWithCombinedAutowired.class,
                Map.ofEntries(
                    getDependency(BeanWithAutowiredConstructor.class),
                    getDependency(BeanWithConstructor.class)
                ),
                "@Autowired constructor and field dependencies are resolved"
            ),
            Arguments.of(BeanWithSetterAutowiring.class,
                Map.ofEntries(
                    getDependency(BeanWithAutowiredConstructor.class),
                    getDependency(BeanWithConstructor.class),
                    getDependency(Double.class)
                ),
                "@Autowired field dependencies are resolved"
            )
        );
    }

    private Entry<String, BeanDependency> getDependency(Class<?> beanType) {
        return Map.entry(
            beanType.getName(),
            new BeanDependency(beanType.getName(), beanType)
        );
    }

    private Stream<Arguments> resolveBeanNames() {
        return Stream.of(
            Arguments.of(BeanWithConstructor.class, BeanWithConstructor.class.getAnnotation(Bean.class).value(),
                "Name is resolved from @Bean annotation value"),
            Arguments.of(BeanWithSetterAutowiring.class, BeanWithSetterAutowiring.class.getName(),
                "Name is resolved from bean type")
        );
    }

    private BeanDefinition getDefinition(Class<?> type, String name, Object instance) {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        doReturn(type).when(beanDefinition).type();
        when(beanDefinition.name()).thenReturn(name);
        when(beanDefinition.getInstance()).thenReturn(instance);

        return beanDefinition;
    }
}
