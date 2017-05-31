package com.github.rerorero.oleoleflake.bitset;

import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.Comparator;

public class StringCodec implements BitSetCodec<String> {

    private final Charset charset;
    private static final BytesCodec byteCodec = BytesCodec.singleton;

    public StringCodec(Charset charset) {
        this.charset = charset;
    }

    public StringCodec() {
        this(Charset.defaultCharset());
    }

    @Override
    public BitSet toBitSet(String value) {
        byte[] bytes = value.getBytes(charset);
        return byteCodec.toBitSet(bytes);
    }

    @Override
    public String toValue(BitSet bit) {
        byte[] bytes = byteCodec.toValue(bit);
        return new String(bytes, charset);
    }

    @Override
    public Comparator<String> comparator() {
        return String.CASE_INSENSITIVE_ORDER;
    }

    @Override
    public Class<String> valueClass() {
        return String.class;
    }
}
