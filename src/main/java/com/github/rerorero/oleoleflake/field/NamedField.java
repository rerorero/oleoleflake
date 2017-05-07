package com.github.rerorero.oleoleflake.field;


import com.github.rerorero.oleoleflake.bitset.BitSetCodec;

public class NamedField<Entire, Field> extends BitSetField<Entire, Field> {

    protected final String name;

    public NamedField(
            int start,
            int size,
            int entireSize,
            BitSetCodec<Entire> entireCodec,
            BitSetCodec<Field> fieldCodec,
            String name,
            boolean invert
    ) {
        super(start, size, entireSize, entireCodec, fieldCodec, invert);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
