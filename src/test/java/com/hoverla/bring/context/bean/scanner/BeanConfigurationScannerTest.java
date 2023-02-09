package com.hoverla.bring.context.bean.scanner;

import com.hoverla.bring.context.bean.definition.BeanDefinition;
import com.hoverla.bring.context.bean.definition.BeanDefinitionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BeanConfigurationScannerTest {

    private static final String CONFIG_PACKAGE_NAME = "com.hoverla.bring.context.fixtures.config";
    private static final String INVALID_PACKAGE_NAME = "com.hoverla.bring.context.fixtures.config.invalid";

    @Mock
    private BeanDefinitionMapper mapper;

    private ConfigurationBeanScanner scanner;

    @Test
    @DisplayName("Successfully scans beans from @Configuration classes in specified packages")
    void scansBeansFromConfigurationClasses() {
        scanner = new ConfigurationBeanScanner(mapper, CONFIG_PACKAGE_NAME, INVALID_PACKAGE_NAME);
        List<BeanDefinition> beanDefinitions = scanner.scan();
        assertThat(beanDefinitions).hasSize(4);
    }

    @Test
    @DisplayName("Returns empty list when @Configuration class is not found")
    void returnsEmptyListIfConfigurationNotFound() {
        scanner = new ConfigurationBeanScanner(mapper, INVALID_PACKAGE_NAME);

        List<BeanDefinition> beanDefinitions = scanner.scan();

        verify(mapper, never()).mapToBeanDefinition(any(), any());
        assertThat(beanDefinitions).isEmpty();
    }

    @Test
    @DisplayName("Returns empty list when we don't pass any package to scan")
    void returnsEmptyListIfThereIsNoPackageToScan() {
        scanner = new ConfigurationBeanScanner(mapper);

        List<BeanDefinition> beanDefinitions = scanner.scan();

        verify(mapper, never()).mapToBeanDefinition(any(), any());
        assertThat(beanDefinitions).isEmpty();
    }
}
