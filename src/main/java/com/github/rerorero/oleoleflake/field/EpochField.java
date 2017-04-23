package com.github.rerorero.oleoleflake.field;

import java.util.Comparator;

/**
 * Warning! Thread Unsafety.
 */
public interface EpochField<Entire, Field> extends BitField<Entire, Field> {

    Comparator<Field> comparator();

    Field currentTimestamp();

    Field lastTimestamp();

    void commitLastTimestamp(Field timestamp);

    Field getTimstampMin();

    Field getTimestampMax();
}
