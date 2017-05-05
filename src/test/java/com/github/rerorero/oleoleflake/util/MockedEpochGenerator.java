package com.github.rerorero.oleoleflake.util;

import com.github.rerorero.oleoleflake.epoch.EpochGenerator;

import java.time.Instant;

public class MockedEpochGenerator extends EpochGenerator<Long> {

    volatile public long timestamp;

    public MockedEpochGenerator(Instant initial) {
         timestamp = initial.getEpochSecond();
    }

    @Override
    public Instant timeGen() {
        return epochToInstant(timestamp);
    }

    @Override
    public Long instantToEpoch(Instant instant) {
        return EpochGenerator.currentTimeSecondsGenerator.instantToEpoch(instant);
    }

    @Override
    public Instant epochToInstant(Long epoch) {
        return EpochGenerator.currentTimeSecondsGenerator.epochToInstant(epoch);
    }
}
