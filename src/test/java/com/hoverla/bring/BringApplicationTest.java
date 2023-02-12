package com.hoverla.bring;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BringApplicationTest {

    @ParameterizedTest(name = "Throws exception when null or empty package name was provided")
    @NullAndEmptySource
    void throwsExceptionWhenNullOrEmptyPackages(String packageName) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> BringApplication.loadContext(packageName));

        assertEquals("Argument [packagesToScan] must not contain null or empty element",
            exception.getMessage());
    }

    @Test
    @DisplayName("Throws exception when no package was provided")
    void throwsExceptionWhenNoPackages() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, BringApplication::loadContext);

        assertEquals("Argument [packagesToScan] must contain at least one not null and not empty element",
            exception.getMessage());
    }

    @ParameterizedTest(name = "Throws exception when package names contain [{0}]")
    @ValueSource(strings = {"^","!","@","#","$","%","^","&","*","(",")","?","~","+","-","<",">","/",","})
    void throwsExceptionWhenInvalidPackageName(String packageName) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> BringApplication.loadContext(packageName));

        assertEquals("Package name must contain only letters, numbers and symbol [.]",
            exception.getMessage());
    }
}
