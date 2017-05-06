package com.github.rerorero.oleoleflake.util;

import com.github.rerorero.oleoleflake.epoch.TimestampGenerator;

import java.time.Instant;

public class MockedTimestampGenerator extends TimestampGenerator<Long> {

    volatile public long timestamp;

    public MockedTimestampGenerator(Instant initial) {
         timestamp = initial.getEpochSecond();
    }

    @Override
    public Instant timeGen() {
        return timestampToInstant(timestamp);
    }

    @Override
    public Long instantToTimestamp(Instant instant) {
        return TimestampGenerator.currentTimeSecondsGenerator.instantToTimestamp(instant);
    }

    @Override
    public Instant timestampToInstant(Long epoch) {
        return TimestampGenerator.currentTimeSecondsGenerator.timestampToInstant(epoch);
    }
}
