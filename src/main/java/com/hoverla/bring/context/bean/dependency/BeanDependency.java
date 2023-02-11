package com.hoverla.bring.context.bean.dependency;

import com.hoverla.bring.annotation.Bean;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;


/**
 * {@link BeanDependency} provide container for {@link Bean} dependencies and methods for build it.
 * <p>
 * Bean dependency its another {@link Bean} or infrastructure object using in current {@link Bean}
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode(of = {"name"})
public class BeanDependency {

    @Setter
    private String name;

    private Class<?> type;

    public static BeanDependency fromParameter(Parameter parameter) {
        Class<?> type = parameter.getType();
        String name = type.getName();
        return new BeanDependency(name, type);
    }

    public static BeanDependency fromField(Field field) {
        Class<?> type = field.getType();
        String name = type.getName();
        return new BeanDependency(name, type);
    }
}
