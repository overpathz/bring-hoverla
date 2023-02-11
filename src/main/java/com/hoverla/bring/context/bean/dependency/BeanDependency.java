package com.hoverla.bring.context.bean.dependency;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

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
