package com.github.rerorero.oleoleflake.bitset;

import java.util.BitSet;

public class BitSetUtil {

    public static BitSet shiftRight(BitSet bitset, int n) {
        return bitset.get(n, Math.max(n, bitset.length()));
    }

    public static BitSet shiftLeft(BitSet bitset, int n) {
        BitSet shifted = new BitSet();
        for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
            shifted.set(i + n);
        }
        return shifted;
    }
}
