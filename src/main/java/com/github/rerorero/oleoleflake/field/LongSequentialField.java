package com.github.rerorero.oleoleflake.field;

import com.github.rerorero.oleoleflake.bitset.BitSetCodec;

/**
 * @note Warning! Thread unsafety.
 */
public class LongSequentialField<Entire> extends LongField<Entire> implements ISequentialField<Entire, Long> {

    private long sequence;
    private final long initValue;
    private final LongSequencer<Entire> sequencer;

    public LongSequentialField(
            int start,
            int size,
            int entireSize,
            BitSetCodec<Entire> entireCodec,
            long initial,
            boolean inverse,
            LongSequencer<Entire> sequencer
    ) {
        super(start, size, entireSize, entireCodec, inverse);
        this.sequence = initial;
        this.initValue = initial;
        this.sequencer = sequencer;
    }

    public static interface LongSequencer<E> {
        long next(long value);
        boolean isLimitOver(long current, LongField<E> field);
    }

    public static <E> LongSequencer<E> incrementalSequencer() {
        return new LongSequencer<E>() {
            @Override
            public long next(long value) {
                return value += 1;
            }

            @Override
            public boolean isLimitOver(long current, LongField<E> field) {
                return current >= field.full();
            }
        };
    }

    @Override
    public Long nextSequence() {
        if (reachedLimit()) {
            resetSequence();
        } else {
            sequence = sequencer.next(sequence);
        }
        return sequence;
    }

    @Override
    public Long currentSequence() {
        return sequence;
    }

    @Override
    public boolean reachedLimit() {
        return sequencer.isLimitOver(sequence, this);
    }

    @Override
    public Long resetSequence() {
        sequence = initialValue();
        return sequence;
    }

    @Override
    public Long initialValue() {
        return initValue;
    }
}
