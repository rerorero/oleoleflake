package com.github.rerorero.oleoleflake.bitset;

import java.util.BitSet;

public interface BitSetCodec<V> {
    BitSet toBitSet(V value);
    V toValue(BitSet bit);
}
