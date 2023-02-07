package com.hoverla.bring.context.fixtures.cache;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.annotation.Cacheable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Bean
public class CachedServiceImpl {

    private int counter = 0;

    @Cacheable("randomNumber")
    public int getRandomNumber(int number, String name) {
        counter++;
        return ThreadLocalRandom.current().nextInt(number);
    }

    public int getCounter() {
        return counter;
    }
}
