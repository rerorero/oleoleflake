package com.github.rerorero.oleoleflake.field;

import com.github.rerorero.oleoleflake.bitset.BitSetCodec;

/**
 * @note Warning! Thread unsafety.
 */
public class LongIncrementalField<Entire> extends LongField<Entire> implements SequentialField<Entire, Long> {

    private long sequence;
    private long initValue;

    public LongIncrementalField(int start, int size, int entireSize, BitSetCodec<Entire> entireCodec, long initial) {
        super(start, size, entireSize, entireCodec);
        this.sequence = initial;
        this.initValue = initial;
    }

    @Override
    public Long nextSequence() {
        if (reachedLimit()) {
            resetSequence();
        } else {
            sequence++;
        }
        return sequence;
    }

    @Override
    public boolean reachedLimit() {
        return sequence >= full();
    }

    @Override
    public Long resetSequence() {
        sequence = initValue;
        return sequence;
    }
}
