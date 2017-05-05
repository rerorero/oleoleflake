package com.github.rerorero.oleoleflake.epoch;

import java.time.Instant;

public abstract class EpochGenerator<T> {

    abstract public Instant timeGen();

    public T timeGenT() {
        return instantToEpoch(timeGen());
    }

    abstract public T instantToEpoch(Instant instant);

    abstract public Instant epochToInstant(T epoch);

    public static final EpochGenerator<Long> currentTimeMillisGenerator = new EpochGenerator<Long>() {
        @Override
        public Instant timeGen() {
            return Instant.now();
        }

        @Override
        public Long instantToEpoch(Instant instant) {
            return instant.toEpochMilli();
        }

        @Override
        public Instant epochToInstant(Long epoch) {
            return Instant.ofEpochMilli(epoch);
        }
    };

    public static final EpochGenerator<Long> currentTimeSecondsGenerator = new EpochGenerator<Long>() {
        @Override
        public Instant timeGen() {
            return Instant.now();
        }

        @Override
        public Long instantToEpoch(Instant instant) {
            return instant.getEpochSecond();
        }

        @Override
        public Instant epochToInstant(Long epoch) {
            return Instant.ofEpochSecond(epoch);
        }
    };
}
