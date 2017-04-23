package com.github.rerorero.oleoleflake.field;

import com.github.rerorero.oleoleflake.bitset.BitSetCodec;

import java.util.Comparator;

/**
 * @note Warning! Thread unsafety!
 */
public class CustomEpochField<Entire> extends LongField<Entire> implements EpochField<Entire, Long> {

    private final long epochBase;
    private final EpochGenerator<Long> epochGenerator;
    private long lastTimestamp;

    public CustomEpochField(int start, int size, int entireSize, BitSetCodec<Entire> entireCodec, long base, EpochGenerator<Long> epochGen) {
        super(start, size, entireSize, entireCodec);
        this.epochBase = base;
        this.lastTimestamp = base;
        this.epochGenerator = epochGen;
    }

    @Override
    public Comparator<Long> comparator() {
        return Comparator.naturalOrder();
    }

    @Override
    public Long currentTimestamp() {
        long timestamp = epochGenerator.timeGen();
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException("Clock moved backrds. current="+timestamp+", last="+lastTimestamp);
        }
        return timestamp;
    }

    @Override
    public Long lastTimestamp() {
        return lastTimestamp;
    }

    @Override
    public void commitLastTimestamp(Long timestamp) {
        this.lastTimestamp = timestamp;
    }

    @Override
    public Long getTimstampMin() {
        return epochBase;
    }

    @Override
    public Long getTimestampMax() {
        return epochBase + full();
    }
}
