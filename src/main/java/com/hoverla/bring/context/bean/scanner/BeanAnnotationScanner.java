package com.hoverla.bring.context.bean.scanner;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.context.bean.definition.BeanDefinitionMapper;
import com.hoverla.bring.context.bean.definition.BeanDefinition;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class BeanAnnotationScanner implements BeanScanner{
    private final BeanDefinitionMapper mapper;
    private final String[] packagesToScan;

    public BeanAnnotationScanner(BeanDefinitionMapper mapper,
                                 String... packagesToScan) {
        this.mapper = mapper;
        this.packagesToScan = packagesToScan;
    }

    @Override
    public List<BeanDefinition> scan() {
        log.info("Starting the scan process of classes annotated with '@Bean' within the '{}' packages",
            Arrays.toString(this.packagesToScan));
        Reflections reflectionScanner = new Reflections((Object[]) this.packagesToScan);
        Set<Class<?>> beanClasses = reflectionScanner.getTypesAnnotatedWith(Bean.class);

        if (beanClasses.isEmpty()) {
            log.warn("No classes annotated with '@Bean' found during the scan in packages {}",
                Arrays.toString(this.packagesToScan));
            return Collections.emptyList();
        }
        log.debug("{} classes annotated with '@Bean' have been found", beanClasses.size());

        //TODO add validation for bean classes
        return beanClasses.stream()
            .map(mapper::mapToBeanDefinition)
            .collect(Collectors.toList());
    }
}
