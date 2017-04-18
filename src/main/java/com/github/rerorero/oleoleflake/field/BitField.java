package com.github.rerorero.oleoleflake.field;

import com.github.rerorero.oleoleflake.OleOleFlakeException;

import java.nio.LongBuffer;
import java.util.BitSet;

public class BitField {
    public final int start;
    public final int size;
    public final int max;
    public static final int nbits = 64;

    protected final BitSet mask;
    private final int bsStart;

    public BitField(int start, int size) {
        this.start = start;
        this.bsStart = bsIndex(start);
        this.size = size;
        BitSet _mask = new BitSet(nbits);
        for (int i = 0; i < size; i++) {
            _mask.set(bsIndex(start + i), true);
        }
        this.mask = _mask;
        this.max = 2 ^ size;
    }

    // convert user index(this.start and this.pos) to BitSet index
    private static int bsIndex(int bitPos) {
        return nbits - bitPos - 1;
    }

    private static long toLong(BitSet bs) throws OleOleFlakeException {
        long[] ary = bs.toLongArray();
        if (ary.length == 1) {
            return ary[0];
        } else if (ary.length == 0) {
            return 0;
        } else {
            throw new OleOleFlakeException("BitSet overflow.");
        }
    }

    public long getFrom(long longValue) throws OleOleFlakeException {
        BitSet bs = BitSet.valueOf(LongBuffer.wrap(new long[]{longValue}))
                    .get(bsStart, bsStart + size);
        return toLong(bs);
    }

    private BitSet longToBitSet(long value) {
        return BitSet.valueOf(LongBuffer.wrap(new long[]{value << (nbits - size - start)}));
    }

    public long setTo(long target, long bitValue) throws OleOleFlakeException {
        BitSet bs = BitSet.valueOf(LongBuffer.wrap(new long[]{target}));
        BitSet targetbs = clearField(bs);
        BitSet valuebs = longToBitSet(bitValue);
        valuebs.and(mask);
        targetbs.or(valuebs);
        return toLong(targetbs);
    }

    protected BitSet clearField(BitSet bs) throws OleOleFlakeException {
        BitSet copy = (BitSet)bs.clone();
        bs.set(bsStart, bsStart + size, false);
        return bs;
    }
}
