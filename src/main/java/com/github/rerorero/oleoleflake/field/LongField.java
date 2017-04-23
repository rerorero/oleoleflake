package com.github.rerorero.oleoleflake.field;

import com.github.rerorero.oleoleflake.OleOleFlakeException;
import com.github.rerorero.oleoleflake.bitset.BitSetCodec;
import com.github.rerorero.oleoleflake.bitset.LongCodec;

public class LongField<Entire> extends BitSetField<Entire, Long> {
    public LongField(int start, int size, int entireSize, BitSetCodec<Entire> entireCodec) {
        super(start, size, entireSize, entireCodec, LongCodec.singleton);
    }

    @Override
    public void validate() throws OleOleFlakeException {
        super.validate();
        if ((start + size) > 64)
            throw new OleOleFlakeException("Invalid bit field length(" + size + "), it's over 64 bit length " + size);
    }
}
