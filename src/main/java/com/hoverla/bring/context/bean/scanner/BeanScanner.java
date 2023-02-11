package com.hoverla.bring.context.bean.scanner;

import com.hoverla.bring.context.bean.definition.BeanDefinition;

import java.util.List;

public interface BeanScanner {
    List<BeanDefinition> scan();
}