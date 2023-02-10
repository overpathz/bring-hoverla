package com.hoverla.bring.context.bean.scanner;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.context.bean.definition.BeanDefinitionMapper;
import com.hoverla.bring.context.bean.definition.BeanDefinition;
import org.reflections.Reflections;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        Reflections reflectionScanner = new Reflections((Object[]) this.packagesToScan);
        Set<Class<?>> beanClasses = reflectionScanner.getTypesAnnotatedWith(Bean.class);

        if (beanClasses.isEmpty()) {
            return Collections.emptyList();
        }

        //TODO add validation for bean classes
        return beanClasses.stream()
            .map(mapper::mapToBeanDefinition)
            .collect(Collectors.toList());
    }
}
