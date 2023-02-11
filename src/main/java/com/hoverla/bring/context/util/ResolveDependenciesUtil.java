package com.hoverla.bring.context.util;

import com.hoverla.bring.context.bean.definition.BeanDefinition;
import com.hoverla.bring.exception.BeanDependencyInjectionException;

import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.hoverla.bring.common.StringConstants.BEAN_HAS_NO_MATCHES_DEPENDENCY_EXCEPTION;
import static java.lang.String.format;

public class ResolveDependenciesUtil {

    private ResolveDependenciesUtil() {
    }

    public static void resolveDependencies(List<BeanDefinition> dependencies, List<Parameter> parameters,
                                     Object[] constructorArgs, String name) {
        Map<Integer, Parameter> params = new LinkedHashMap<>();
        for (int i = 0; i < parameters.size(); i++) {
            params.put(i, parameters.get(i));
        }
        params
            .entrySet()
            .stream()
            .sorted(ResolveDependenciesUtil::subclassesFirst)
            .forEachOrdered(entry -> resolveArgumentFromEntry(entry, dependencies, constructorArgs, name));
    }

    private static int subclassesFirst(Map.Entry<Integer, Parameter> parameterEntry1,
                                       Map.Entry<Integer, Parameter> parameterEntry2) {
        Class<?> type1 = parameterEntry1.getValue().getType();
        Class<?> type2 = parameterEntry2.getValue().getType();
        if (type1.isAssignableFrom(type2)) {
            return 1;
        }
        return -1;
    }

    private static void resolveArgumentFromEntry(Map.Entry<Integer, Parameter> indexedParameter,
                                          List<BeanDefinition> dependencies, Object[] constructorArgs, String name) {
        Integer index = indexedParameter.getKey();
        Parameter parameter = indexedParameter.getValue();
        Object matchingArgument = getBeanDefinitionByParameter(parameter, dependencies, name).getInstance();
        constructorArgs[index] = matchingArgument;
    }

    private static BeanDefinition getBeanDefinitionByParameter(Parameter parameter, List<BeanDefinition> dependencies,
                                                               String name) {
        BeanDefinition beanDefinition = dependencies.stream()
            .filter(bd -> parameterMatch(parameter, bd))
            .findFirst()
            .orElseThrow(() -> new BeanDependencyInjectionException(format(
                    BEAN_HAS_NO_MATCHES_DEPENDENCY_EXCEPTION, name, parameter.getType().getName())));

        dependencies.remove(beanDefinition);
        return beanDefinition;
    }

    private static boolean parameterMatch(Parameter parameter, BeanDefinition dependencyToMatch) {
        Class<?> parameterType = parameter.getType();
        return parameterType.isAssignableFrom(dependencyToMatch.type());
    }
}
