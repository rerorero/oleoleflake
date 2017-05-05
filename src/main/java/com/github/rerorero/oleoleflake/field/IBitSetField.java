package com.github.rerorero.oleoleflake.field;

import java.util.Comparator;

public interface IBitSetField<Entire, Field> extends IField {
    Field full();

    Field zero();

    Field getField(Entire entire);

    Entire putField(Entire entire, Field value);

    Comparator<Entire> entireComparator();

    Comparator<Field> fieldComparator();
}
