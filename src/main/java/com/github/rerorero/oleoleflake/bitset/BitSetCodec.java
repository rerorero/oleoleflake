package com.github.rerorero.oleoleflake.bitset;

import java.util.BitSet;
import java.util.Comparator;

public interface BitSetCodec<V> {
    BitSet toBitSet(V value);

    V toValue(BitSet bit);

    Comparator<V> comparator();

    Class<V> valueClass();
}
