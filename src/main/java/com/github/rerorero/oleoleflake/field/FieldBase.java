package com.github.rerorero.oleoleflake.field;

import com.github.rerorero.oleoleflake.OleOleFlakeException;

public class FieldBase implements IField {
    protected final int start;
    protected final int size;
    protected final int entireSize;

    public FieldBase(int start, int size, int entireSize) {
        this.start = start;
        this.size = size;
        this.entireSize = entireSize;
    }

    public void validate() {
        if (size < 0 )
            throw new OleOleFlakeException("Invalid bit field length: " + size);
        if ((start < 0) || (entireSize < start))
            throw new OleOleFlakeException("Bit field start position("+start+") is out of range(between 0 to " + entireSize + ")");
        if ((start + size) > entireSize)
            throw new OleOleFlakeException("Invalid bit field length(" + size + "), it's over entire bit length " + size);
    }

    public int getStartBit() {
        return start;
    }

    public int getFieldSize() {
        return size;
    }

    public int getEntireSize() {
        return entireSize;
    }
}
