package com.github.rerorero.oleoleflake.bitset;

import com.github.rerorero.oleoleflake.util.ByteUtil;

import java.util.BitSet;
import java.util.Comparator;

public class BytesCodec implements BitSetCodec<byte[]> {

    public static BytesCodec singleton = new BytesCodec();

    private static final Comparator<byte[]> bytesComparator = new Comparator<byte[]>() {
        @Override
        public int compare(byte[] left, byte[] right) {
            for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
                int a = (left[i] & 0xff);
                int b = (right[j] & 0xff);
                if (a != b) {
                    return a - b;
                }
            }
            return left.length - right.length;
        }
    };

    @Override
    public BitSet toBitSet(byte[] value) {
        byte[] reversed = ByteUtil.reverse(value.clone());
        return BitSet.valueOf(reversed);
    }

    @Override
    public byte[] toValue(BitSet bit) {
        return ByteUtil.reverse(bit.toByteArray());
    }

    @Override
    public Comparator<byte[]> comparator() {
        return bytesComparator;
    }

    @Override
    public Class<byte[]> valueClass() {
        return byte[].class;
    }
}
