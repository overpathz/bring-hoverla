package com.hoverla.bring;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.hoverla.bring.context.ApplicationContext;
import com.hoverla.bring.context.DefaultApplicationContextImpl;
import com.hoverla.bring.context.bean.definition.BeanDefinitionMapper;
import com.hoverla.bring.context.bean.dependency.BeanDependencyNameResolver;
import com.hoverla.bring.context.bean.initializer.BeanInitializer;
import com.hoverla.bring.context.bean.scanner.BeanAnnotationScanner;
import com.hoverla.bring.context.bean.scanner.BeanScanner;
import com.hoverla.bring.context.bean.scanner.ConfigurationBeanScanner;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * <pre>
 * Bring starting point. Initializes and returns {@link ApplicationContext} encapsulating
 * all necessary logic for its creation.
 *
 * Usage:
 * {@code
 *     ApplicationContext context = BringApplication.loadContext("com.hoverla.bring");
 *     String bean = context.getBean("bean_name", String.class);
 * }
 * </pre>
 * <p>
 * BringApplication provides possibility to define log level. Use {@link ApplicationContextBuilder} for this purpose.
 * <p>
 * Usage:
 * {@code
 *
 *
 * }
 *
 * It is necessary to provide packages to be scanned to define bean definition configs.
 */
@UtilityClass
public class BringApplication {

    private static final CharSequence[] ILLEGAL_SYMBOLS = {"^","!","@","#","$","%","^","&","*","(",")","?","~","+","-","<",">","/",","};

    public static ApplicationContext loadContext(String... packagesToScan) {
        return createContext(packagesToScan);
    }

    private ApplicationContext createContext(String... packagesToScan) {
        validatePackagesToScan(packagesToScan);

        List<BeanScanner> scanners = createBeanScanners(packagesToScan);
        var dependencyNameResolver = new BeanDependencyNameResolver();
        var initializer = new BeanInitializer(dependencyNameResolver);

        return new DefaultApplicationContextImpl(scanners, initializer);
    }

    private List<BeanScanner> createBeanScanners(String[] packagesToScan) {
        var beanDefinitionMapper = new BeanDefinitionMapper();
        var beanAnnotationScanner = new BeanAnnotationScanner(beanDefinitionMapper, packagesToScan);
        var beanConfigurationClassScanner = new ConfigurationBeanScanner(beanDefinitionMapper, packagesToScan);

        return List.of(beanAnnotationScanner, beanConfigurationClassScanner);
    }

    private static void validatePackagesToScan(String... packagesToScan) {
        if (ArrayUtils.isEmpty(packagesToScan)) {
            throw new IllegalArgumentException("Argument [packagesToScan] must contain at least one not null and not empty element");
        }
        if (Arrays.stream(packagesToScan)
            .anyMatch(StringUtils::isBlank)) {
            throw new IllegalArgumentException("Argument [packagesToScan] must not contain null or empty element");
        }
        if (Arrays.stream(packagesToScan)
            .anyMatch(p -> StringUtils.containsAny(p, ILLEGAL_SYMBOLS))) {
            throw new IllegalArgumentException("Package name must contain only letters, numbers and symbol [.]");
        }
    }

    public ApplicationContextBuilder getContextBuilder() {
        return new ApplicationContextBuilder();
    }

    public class ApplicationContextBuilder {

        private Level logLevel;
        private String[] packagesToScan;

        public ApplicationContextBuilder logLevel(Level logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public ApplicationContextBuilder packagesToScan(String... packagesToScan) {
            this.packagesToScan = packagesToScan;
            return this;
        }

        public ApplicationContext build() {
            Logger logger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            logger.setLevel(logLevel != null ? logLevel : Level.INFO);
            return createContext(packagesToScan);
        }
    }
}
