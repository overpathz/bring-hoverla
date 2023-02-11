package com.hoverla.bring.context.bean.dependency;

import com.hoverla.bring.context.bean.definition.BeanDefinition;
import com.hoverla.bring.context.bean.definition.BeanDefinitionContainer;
import com.hoverla.bring.exception.BeanInstanceCreationException;
import com.hoverla.bring.exception.MissingDependencyException;
import com.hoverla.bring.exception.NoUniqueBeanException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.hoverla.bring.common.StringConstants.BEAN_INSTANCE_CREATION_SAME_CANDIDATE_EXCEPTION;
import static com.hoverla.bring.common.StringConstants.MISSING_DEPENDENCY_EXCEPTION;
import static com.hoverla.bring.common.StringConstants.NO_UNIQUE_BEAN_EXCEPTION;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

/**
 * {@link BeanDependencyNameResolver} its util class using to resolve {@link BeanDependency} names.
 */
@Slf4j
public class BeanDependencyNameResolver {
    public void resolveDependencyNames(BeanDefinitionContainer container) {
        log.debug("Resolving dependency names for each bean definition before initialization");

        for (BeanDefinition beanDefinition : container.getBeanDefinitions()) {
            Map<String, BeanDependency> beanDependencies = beanDefinition.dependencies();

            if (beanDependencies.isEmpty()) {
                continue;
            }

            log.trace("Verifying names of {} dependencies of bean definition {} - {} ",
                beanDependencies.size(), beanDefinition.name(), beanDefinition.type().getName());

            List<Pair<String, String>> oldToNewNames = beanDependencies.values()
                .stream()
                .map(dependency -> resolveDependencyName(dependency, beanDefinition, container))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            Map<String, BeanDependency> oldDependencies = Map.copyOf(beanDependencies);

            oldToNewNames.forEach(namePair -> replaceOldName(namePair, beanDependencies));
            if (oldDependencies.size() != beanDependencies.size()) {
                throw new BeanInstanceCreationException(format(BEAN_INSTANCE_CREATION_SAME_CANDIDATE_EXCEPTION, beanDefinition.name()));
            }
        }
    }

    private void replaceOldName(Pair<String, String> oldNameToNewName, Map<String, BeanDependency> beanDependencies) {
        String oldName = oldNameToNewName.getLeft();
        String newName = oldNameToNewName.getRight();
        BeanDependency oldDependency = beanDependencies.remove(oldName);
        oldDependency.setName(newName);
        beanDependencies.put(newName, oldDependency);
    }

    @Nullable
    private Pair<String, String> resolveDependencyName(BeanDependency targetDependency,
                                                       BeanDefinition rootDefinition,
                                                       BeanDefinitionContainer container) {
        String dependencyName = targetDependency.getName();
        Class<?> dependencyType = targetDependency.getType();

        if (container.getBeanDefinitionByName(dependencyName).isPresent()) {
            return null;
        }

        log.debug("Resolving dependency: {}. Trying to find the matching bean definition by type: {} ",
            dependencyName, dependencyType.getName());


        List<BeanDefinition> sameTypeBeans = container.getBeansWithExactType(dependencyType);

        List<BeanDefinition> sameTypeBeansCopy = new ArrayList<>(sameTypeBeans);
        sameTypeBeansCopy.remove(rootDefinition);

        Optional<BeanDefinition> optionalDependency;
        optionalDependency = findMatchingDependencyFirstTry(sameTypeBeansCopy);

        BeanDefinition matchingDependency;
        if (optionalDependency.isEmpty()) {
            List<BeanDefinition> assignableBeans = container.getBeansAssignableFromType(dependencyType);

            List<BeanDefinition> assignableBeansCopy = new ArrayList<>(assignableBeans);
            assignableBeansCopy.remove(rootDefinition);

            matchingDependency = findMatchingDependency(assignableBeansCopy, targetDependency, rootDefinition);
        } else {
            matchingDependency = optionalDependency.orElseThrow(() ->
                new MissingDependencyException(String.format(MISSING_DEPENDENCY_EXCEPTION, dependencyType, dependencyName,
                    rootDefinition.name())));
        }

        String newDependencyName = matchingDependency.name();
        if (newDependencyName.equals(dependencyName)) {
            log.debug("Replacement is not needed. Dependency has the same default name of field/parameter");
            return null;
        }

        log.debug("Replacing the default name '{}' with '{}' for bean definition with type {}, which is a dependency of " +
                "bean with name '{}' and type {}",
            dependencyName, newDependencyName, matchingDependency.type().getName(), rootDefinition.name(),
            rootDefinition.type().getName());

        return Pair.of(dependencyName, newDependencyName);
    }

    private Supplier<String> getMatchingBeanMessage(List<BeanDefinition> beanDefinitions) {
        return () -> beanDefinitions.stream()
            .map(beanDefinition -> beanDefinition.name() + ": " + beanDefinition.type().getSimpleName())
            .collect(joining(", "));
    }


    private Optional<BeanDefinition> findMatchingDependencyFirstTry(List<BeanDefinition> dependencies) {
        if (dependencies.size() > 1) {
            return dependencies.stream()
                .filter(BeanDefinition::isPrimary)
                .findFirst();
        }
        return dependencies.stream().findFirst();
    }
    private BeanDefinition findMatchingDependency(List<BeanDefinition> dependencies, BeanDependency targetDependency,
                                                  BeanDefinition rootDefinition) {
        if (dependencies.size() > 1) {
            log.trace("Found more than one matching bean definition. Need to find the @Primary bean definition");
            Supplier<String> errorMessageSupplier = getMatchingBeanMessage(dependencies);

            return dependencies.stream()
                .filter(BeanDefinition::isPrimary)
                .findFirst()
                .orElseThrow(() -> new NoUniqueBeanException(format(NO_UNIQUE_BEAN_EXCEPTION,
                    targetDependency.getType().getSimpleName(), errorMessageSupplier.get())));
        }
        return dependencies.stream().findFirst().orElseThrow(() ->
            new MissingDependencyException(String.format(MISSING_DEPENDENCY_EXCEPTION, targetDependency.getType(),
                targetDependency.getName(),
                rootDefinition.name())));
    }
}