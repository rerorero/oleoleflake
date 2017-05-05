package com.github.rerorero.oleoleflake.bitset;

import java.util.BitSet;
import java.util.Comparator;

public class LongCodec implements BitSetCodec<Long> {

    public static LongCodec singleton = new LongCodec();

    @Override
    public BitSet toBitSet(Long value) {
        return BitSet.valueOf(new long[]{value});
    }

    @Override
    public Long toValue(BitSet bit) {
        long[] ary = bit.toLongArray();
        if (ary.length == 1) {
            return ary[0];
        } else if (ary.length == 0) {
            return 0L;
        } else {
            throw new IllegalArgumentException("BitSet overflow.: " + ary.length);
        }
    }

    @Override
    public Comparator<Long> comparator() {
        return Comparator.naturalOrder();
    }

    @Override
    public Class<Long> valueClass() {
        return Long.class;
    }
}
