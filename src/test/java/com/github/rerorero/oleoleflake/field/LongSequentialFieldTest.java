package com.github.rerorero.oleoleflake.field;

import com.github.rerorero.oleoleflake.bitset.LongCodec;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LongSequentialFieldTest {
    @Test
    public void nextSequenceTest() {
        LongSequentialField<Long> sut1 = new LongSequentialField(4, 4, 8, LongCodec.singleton, 0L, false, LongSequentialField.incrementalSequencer());
        assertEquals(Long.valueOf(0), sut1.currentSequence());
        for(int i = 0; i < 15; i++) {
            assertEquals(false, sut1.reachedLimit());
            assertEquals(Long.valueOf(i+1), sut1.nextSequence());
            assertEquals(Long.valueOf(i+1), sut1.currentSequence());
        }
        assertEquals(true, sut1.reachedLimit());
        assertEquals(Long.valueOf(0), sut1.nextSequence());
        assertEquals(false, sut1.reachedLimit());
    }

    @Test
    public void resetSequenceTest() {
        LongSequentialField<Long> sut1 = new LongSequentialField(4, 4, 8, LongCodec.singleton, 0L, false, LongSequentialField.incrementalSequencer());
        assertEquals(Long.valueOf(0), sut1.currentSequence());
        assertEquals(Long.valueOf(1), sut1.nextSequence());
        sut1.resetSequence();
        assertEquals(Long.valueOf(0), sut1.currentSequence());
        assertEquals(Long.valueOf(1), sut1.nextSequence());
    }
}
