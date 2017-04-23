package com.github.rerorero.oleoleflake.field;

import com.github.rerorero.oleoleflake.OleOleFlakeException;
import com.github.rerorero.oleoleflake.bitset.BitSetCodec;
import com.github.rerorero.oleoleflake.bitset.BitSetUtil;

import java.util.BitSet;

public abstract class BitSetField<Entire, Field> implements BitField<Entire, Field> {
    protected final int start;
    protected final int size;
    protected final int max;
    protected final int entireSize;
    private final BitSetCodec<Entire> entireCodec;
    private final BitSetCodec<Field> fieldCodec;
    protected final String detail;

    protected final BitSet mask;
    private final int bsStart;

    public BitSetField(int start, int size, int entireSize, BitSetCodec<Entire> entireCodec, BitSetCodec<Field> fieldCodec) {
        this.start = start;
        this.bsStart = entireSize - start - size;
        this.size = size;
        this.entireSize = entireSize;
        this.entireCodec = entireCodec;
        this.fieldCodec = fieldCodec;

        BitSet _mask = new BitSet(this.entireSize);
        for (int i = 0; i < size; i++) {
            _mask.set(entireSize - (start + i) - 1, true);
        }
        this.mask = _mask;
        this.max = 2 ^ size;

        detail = new StringBuilder()
            .append(this.getClass().getSimpleName())
            .append("(")
            .append("start=" + start)
            .append(",size=" + size)
            .append(",bsStart=" + bsStart)
            .append(",entireSizes=" + entireSize)
            .append(",Entire=" + entireCodec.getClass().getSimpleName())
            .append(",Field=" + fieldCodec.getClass().getSimpleName())
            .append(")")
            .toString();
    }

    @Override
    public String toString() {
        return detail;
    }

    public Field getField(Entire entire) {
        BitSet entireBs = entireCodec.toBitSet(entire);
        BitSet field = getFieldAsBit(entireBs);
        return fieldCodec.toValue(field);
    }

    protected BitSet getFieldAsBit(BitSet entire) {
        entire.and(mask);
        return BitSetUtil.shiftRight(entire, bsStart);
    }

    public Entire putField(Entire entire, Field value) {
        BitSet entireBs = entireCodec.toBitSet(entire);
        BitSet bs = fieldCodec.toBitSet(value);
        putFieldAsBit(entireBs, bs);
        return entireCodec.toValue(entireBs);
    }

    protected void putFieldAsBit(BitSet entire, BitSet value) {
        BitSet shifted = BitSetUtil.shiftLeft(value, entireSize - size - start);
        shifted.and(mask);
        putFieldZero(entire);
        entire.or(shifted);
    }

    protected void putFieldZero(BitSet entire) {
        entire.set(bsStart, bsStart + size, false);
    }

    public void validate() throws OleOleFlakeException {
        if (size < 0 )
            throw new OleOleFlakeException("Invalid bit field length: " + size);
        if ((start < 0) || (entireSize < start))
            throw new OleOleFlakeException("Bit field start position("+start+") is out of range(between 0 to " + entireSize + ")");
        if ((start + size) > entireSize)
            throw new OleOleFlakeException("Invalid bit field length(" + size + "), it's over entire bit length " + size);
    }

    public Field zero() {
        return fieldCodec.toValue(new BitSet());
    }

    public Field full() {
        BitSet fullBitset = new BitSet();
        fullBitset.set(0, size, true);
        return fieldCodec.toValue(fullBitset);
    }
}

