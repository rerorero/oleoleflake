package com.github.rerorero.oleoleflake.field;

public interface SequentialField<Entire, Field> extends BitField<Entire, Field> {
    Field nextSequence();

    boolean reachedLimit();

    Field resetSequence();
}
