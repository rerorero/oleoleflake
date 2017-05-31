package com.github.rerorero.oleoleflake.bitset;

import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;

public class BitSetUtilTest {

    @Test
    public void shiftRightTest() {
        assertEquals(
                BitSet.valueOf(new long[]{ 0b00101011L >>  3 }),
                BitSetUtil.shiftRight(BitSet.valueOf(new long[]{ 0b00101011L }), 3));

        assertEquals(
                BitSet.valueOf(new long[]{ (0b11111L >> 3) | (0b011L << 61), 0b11011L >> 3 }),
                BitSetUtil.shiftRight(BitSet.valueOf(new long[]{ 0b11111L, 0b11011L }), 3));
    }

    @Test
    public void shiftLeftTest() {
        assertEquals(
                BitSet.valueOf(new long[]{ 0b00101011L << 3 }),
                BitSetUtil.shiftLeft(BitSet.valueOf(new long[]{ 0b00101011L }), 3));
        assertEquals(
                BitSet.valueOf(new long[]{ 0b11111L << 3, 0b11011L << 3 }),
                BitSetUtil.shiftLeft(BitSet.valueOf(new long[]{ 0b11111L, 0b11011L }), 3));
    }
}
