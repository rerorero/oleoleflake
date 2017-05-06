package com.github.rerorero.oleoleflake.epoch;

import java.time.Instant;

public abstract class TimestampGenerator<T> {

    abstract public Instant timeGen();

    public T timeGenT() {
        return instantToTimestamp(timeGen());
    }

    abstract public T instantToTimestamp(Instant instant);

    abstract public Instant timestampToInstant(T epoch);

    public static final TimestampGenerator<Long> currentTimeMillisGenerator = new TimestampGenerator<Long>() {
        @Override
        public Instant timeGen() {
            return Instant.now();
        }

        @Override
        public Long instantToTimestamp(Instant instant) {
            return instant.toEpochMilli();
        }

        @Override
        public Instant timestampToInstant(Long epoch) {
            return Instant.ofEpochMilli(epoch);
        }
    };

    public static final TimestampGenerator<Long> currentTimeSecondsGenerator = new TimestampGenerator<Long>() {
        @Override
        public Instant timeGen() {
            return Instant.now();
        }

        @Override
        public Long instantToTimestamp(Instant instant) {
            return instant.getEpochSecond();
        }

        @Override
        public Instant timestampToInstant(Long epoch) {
            return Instant.ofEpochSecond(epoch);
        }
    };
}
