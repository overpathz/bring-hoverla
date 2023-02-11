package com.hoverla.bring.context.bean.scanner;

import com.hoverla.bring.context.bean.definition.BeanDefinition;
import com.hoverla.bring.context.bean.definition.BeanDefinitionMapper;
import com.hoverla.bring.context.fixtures.bean.success.A;
import com.hoverla.bring.context.fixtures.bean.success.B;
import com.hoverla.bring.context.fixtures.bean.success.ChildServiceBeanOne;
import com.hoverla.bring.context.fixtures.bean.success.ChildServiceBeanTwo;
import com.hoverla.bring.context.fixtures.bean.success.TestBeanWithName;
import com.hoverla.bring.context.fixtures.bean.success.TestBeanWithoutName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeanAnnotationScannerTest {

    private static final String TEST_PACKAGE_TO_SCAN = "com.hoverla.bring.context.fixtures.bean.success";
    private static final String INVALID_PACKAGE_TO_SCAN = "com.hoverla.bring.context.fixtures.bean.invalid";

    @Mock
    private BeanDefinitionMapper mapper;

    @Test
    @DisplayName("Scans all beans from a given package, validates those classes and maps them to bean definitions")
    void testScan() {
        Set<Class<?>> expectedBeanClasses = Set.of(A.class, B.class, ChildServiceBeanOne.class,
            ChildServiceBeanTwo.class, TestBeanWithName.class, TestBeanWithoutName.class);
        for (Class<?> beanClass : expectedBeanClasses) {
            BeanDefinition beanDefinition = getBeanDefinition(beanClass);
            when(mapper.mapToBeanDefinition(beanClass)).thenReturn(beanDefinition);
        }

        var beanAnnotationScanner = new BeanAnnotationScanner(mapper, TEST_PACKAGE_TO_SCAN);
        List<BeanDefinition> resolvedDefinitions = beanAnnotationScanner.scan();

        verify(mapper, times(expectedBeanClasses.size())).mapToBeanDefinition(any(Class.class));


        assertEquals(expectedBeanClasses.size(), resolvedDefinitions.size());
        assertTrue(resolvedDefinitions.stream().map(BeanDefinition::type).allMatch(expectedBeanClasses::contains));
    }

    @Test
    @DisplayName("Scan returns empty collection, since there are no classes marked with @Bean. Validation and mapping don't happen")
    void testScanWithoutBeans() {
        var beanAnnotationScanner = new BeanAnnotationScanner(mapper, INVALID_PACKAGE_TO_SCAN);
        List<BeanDefinition> scannedDefinitions = beanAnnotationScanner.scan();

        verify(mapper, never()).mapToBeanDefinition(any(Class.class));

        assertThat(scannedDefinitions).isEmpty();
    }

    private BeanDefinition getBeanDefinition(Class<?> beanClass) {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        doReturn(beanClass).when(beanDefinition).type();
        return beanDefinition;
    }
}
