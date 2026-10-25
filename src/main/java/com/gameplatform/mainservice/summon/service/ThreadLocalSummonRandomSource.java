package com.gameplatform.mainservice.summon.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class ThreadLocalSummonRandomSource implements SummonRandomSource {

    @Override
    public int nextInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }
}
