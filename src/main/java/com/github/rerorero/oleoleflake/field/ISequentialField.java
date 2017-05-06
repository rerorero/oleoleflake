package com.github.rerorero.oleoleflake.field;

public interface ISequentialField<Entire, Field> extends IBitSetField<Entire, Field> {
    Field nextSequence();

    Field currentSequence();

    boolean reachedLimit();

    Field resetSequence();

    Field initialValue();
}
