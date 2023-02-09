package com.hoverla.bring.context;

import com.hoverla.bring.context.fixtures.cache.CachedServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CacheableTest {
    @Test
    @Order(1)
    @DisplayName("Cache works properly")
    void cacheableMethod() {
        ApplicationContext autowiringContext =
                new AnnotationApplicationContextImpl("com.hoverla.bring.context.fixtures.cache");
        CachedServiceImpl cachedServiceImpl = autowiringContext.getBean(CachedServiceImpl.class);
        int number = cachedServiceImpl.getRandomNumber(1, "name");
        assertEquals(number, cachedServiceImpl.getRandomNumber(1, "name"));
        assertEquals(number, cachedServiceImpl.getRandomNumber(1, "name"));
        assertEquals(1, cachedServiceImpl.getCounter());
    }

    @Test
    @Order(2)
    @DisplayName("Cache works properly for method with 2 arguments")
    void cacheableMethodWith2Arguments() {
        ApplicationContext autowiringContext =
                new AnnotationApplicationContextImpl("com.hoverla.bring.context.fixtures.cache");
        CachedServiceImpl cachedServiceImpl = autowiringContext.getBean(CachedServiceImpl.class);
        int number = cachedServiceImpl.getRandomNumber(1, "name");
        assertEquals(number, cachedServiceImpl.getRandomNumber(1, "name"));
        assertNotEquals(number, cachedServiceImpl.getRandomNumber(2, "name"));
        assertEquals(2, cachedServiceImpl.getCounter());
    }
}
