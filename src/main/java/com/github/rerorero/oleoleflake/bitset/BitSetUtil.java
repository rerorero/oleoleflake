package com.github.rerorero.oleoleflake.bitset;

import java.util.BitSet;
import java.util.Comparator;

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

    public static final Comparator<BitSet> bitsetComparator = new Comparator<BitSet>() {
        @Override
        public int compare(BitSet left, BitSet right) {
            if (right.equals(left)) return 0;
            BitSet xor = (BitSet)right.clone();
            xor.xor(left);
            int firstDifferent = xor.length()-1;
            if(firstDifferent==-1)
                return 0;

            return left.get(firstDifferent) ? 1 : -1;
        }
    };

    public static final BitSetCodec bitsetCodec = new BitSetCodec<BitSet>() {

        @Override
        public BitSet toBitSet(BitSet value) {
            return value;
        }

        @Override
        public BitSet toValue(BitSet bit) {
            return bit;
        }

        @Override
        public Comparator<BitSet> comparator() {
            return bitsetComparator;
        }

        @Override
        public Class<BitSet> valueClass() {
            return BitSet.class;
        }
    };
}
