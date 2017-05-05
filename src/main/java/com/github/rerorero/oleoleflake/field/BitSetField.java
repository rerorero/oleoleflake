package com.github.rerorero.oleoleflake.field;

import com.github.rerorero.oleoleflake.bitset.BitSetCodec;
import com.github.rerorero.oleoleflake.bitset.BitSetUtil;

import java.util.BitSet;
import java.util.Comparator;

public abstract class BitSetField<Entire, Field> extends FieldBase implements IBitSetField<Entire, Field> {
    protected final int max;
    protected final BitSetCodec<Entire> entireCodec;
    protected final BitSetCodec<Field> fieldCodec;
    protected final String detail;
    protected final boolean inverse;

    protected final BitSet mask;
    private final int bsStart;

    public BitSetField(int start, int size, int entireSize, BitSetCodec<Entire> entireCodec, BitSetCodec<Field> fieldCodec, boolean inverse) {
        super(start,size,entireSize);
        this.bsStart = entireSize - start - size;
        this.entireCodec = entireCodec;
        this.fieldCodec = fieldCodec;
        this.inverse = inverse;

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
            .append(",inverse=" + inverse)
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

    private BitSet inverseIfNeed(BitSet bs) {
        if (inverse)
            bs.flip(0, size);
        return bs;
    }

    public Field getField(Entire entire) {
        BitSet entireBs = entireCodec.toBitSet(entire);
        BitSet field = getFieldAsBit(entireBs);
        return fieldCodec.toValue(field);
    }

    protected BitSet getFieldAsBit(BitSet entire) {
        entire.and(mask);
        return inverseIfNeed(BitSetUtil.shiftRight(entire, bsStart));
    }

    public Entire putField(Entire entire, Field value) {
        BitSet entireBs = entireCodec.toBitSet(entire);
        BitSet bs = fieldCodec.toBitSet(value);
        putFieldAsBit(entireBs, inverseIfNeed(bs));
        return entireCodec.toValue(entireBs);
    }

    protected void putFieldAsBit(BitSet entire, BitSet value) {
        BitSet shifted = BitSetUtil.shiftLeft(value, entireSize - size - start);
        shifted.and(mask);
        putZeroIntoField(entire);
        entire.or(shifted);
    }

    protected void putZeroIntoField(BitSet entire) {
        entire.set(bsStart, bsStart + size, false);
    }

    @Override
    public Field zero() {
        return fieldCodec.toValue(new BitSet());
    }

    @Override
    public Field full() {
        BitSet fullBitset = new BitSet();
        fullBitset.set(0, size, true);
        return fieldCodec.toValue(fullBitset);
    }

    @Override
    public Comparator<Field> fieldComparator() {
        return fieldCodec.comparator();
    }

    @Override
    public Comparator<Entire> entireComparator() {
        return entireCodec.comparator();
    }
}

