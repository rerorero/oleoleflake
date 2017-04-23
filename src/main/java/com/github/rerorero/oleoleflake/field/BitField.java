package com.github.rerorero.oleoleflake.field;

import com.github.rerorero.oleoleflake.OleOleFlakeException;

public interface BitField<Entire, Field> {
    Field full();

    Field zero();

    void validate() throws OleOleFlakeException;

    Field getField(Entire entire);

    Entire putField(Entire entire, Field value);

}
