package com.github.rerorero.oleoleflake.field;

public class FixedField extends BitField {

    public final long fixedValue;

    public FixedField(int start, int size, long fixed) {
        super(start, size);
        this.fixedValue = fixed;
    }
}