package com.hoverla.bring.context.bean.definition;

import com.hoverla.bring.context.fixtures.bean.success.TestBeanWithoutName;
import com.hoverla.bring.context.fixtures.config.TestConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BeanDefinitionMapperTest {

    private final BeanDefinitionMapper mapper = new BeanDefinitionMapper();

    @Test
    @DisplayName("Configuration class bean is mapped to ConfigurationBeanDefinition")
    void configurationClassIsMappedToConfigurationBeanDefinition() throws NoSuchMethodException {
        TestConfiguration beanConfigClass = new TestConfiguration();
        Method beanMethod = beanConfigClass.getClass().getMethod("namedBean");

        BeanDefinition beanDefinition = mapper.mapToBeanDefinition(beanConfigClass, beanMethod);

        assertTrue(beanDefinition instanceof ConfigurationBeanDefinition);
    }

    @Test
    @DisplayName("Default bean is mapped to DefaultBeanDefinition")
    void defaultBeanIsMappedToDefaultBeanDefinition() {
        BeanDefinition beanDefinition = mapper.mapToBeanDefinition(TestBeanWithoutName.class);

        assertTrue(beanDefinition instanceof DefaultBeanDefinition);
    }
}
