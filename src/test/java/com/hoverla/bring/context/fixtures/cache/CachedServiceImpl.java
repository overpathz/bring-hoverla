package com.hoverla.bring.context.fixtures.cache;

import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.annotation.Cacheable;
import com.hoverla.bring.annotation.TimeLog;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Bean
public class CachedServiceImpl {

    private int counter = 0;

    @TimeLog
    @Cacheable("randomNumber")
    public int getRandomNumber(int number, String name) {
        counter++;
        return ThreadLocalRandom.current().nextInt(number * 10000);
    }

    public int getCounter() {
        return counter;
    }
}
