package com.github.rerorero.oleoleflake.field;

import java.time.ZonedDateTime;

public abstract class EpochGenerator<T> {

    abstract public T timeGen();

    public static final EpochGenerator<Long> currentTimeMillisGenerator = new EpochGenerator<Long>() {
        @Override
        public Long timeGen() {
            return System.currentTimeMillis();
        }
    };

    public static final EpochGenerator<Long> currentTimeSecondsGenerator = new EpochGenerator<Long>() {
        @Override
        public Long timeGen() {
            return ZonedDateTime.now().toEpochSecond();
        }
    };
}
