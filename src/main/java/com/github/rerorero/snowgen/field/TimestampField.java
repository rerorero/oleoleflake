package com.github.rerorero.snowgen.field;

public class TimestampField extends BitField {
    public final long epoc;

    public TimestampField(int start, int size, long epoc) {
        super(start, size);
        this.epoc = epoc;
    }
}
