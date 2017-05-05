package com.github.rerorero.oleoleflake.field;

import com.github.rerorero.oleoleflake.bitset.BitSetCodec;
import com.github.rerorero.oleoleflake.epoch.EpochGenerator;

import java.time.Instant;

/**
 * @note Warning! Thread unsafety!
 */
public class EpochField<Entire> extends LongField<Entire> {

    private final long epochBase;
    private final EpochGenerator<Long> epochGenerator;
    private long lastTimestamp;

    public EpochField(
            int start,
            int size,
            int entireSize,
            BitSetCodec<Entire> entireCodec,
            long origin,
            EpochGenerator<Long> epochGen,
            boolean inverse
    ) {
        super(start, size, entireSize, entireCodec, inverse);
        this.epochBase = origin;
        this.lastTimestamp = origin;
        this.epochGenerator = epochGen;
    }

    public Long currentTimestamp() {
        long timestamp = epochGenerator.timeGenT();
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException("Clock moved backrds. current="+timestamp+", last="+lastTimestamp);
        }
        return timestamp;
    }

    public Long lastTimestamp() {
        return lastTimestamp;
    }

    public void commitLastTimestamp(Long timestamp) {
        this.lastTimestamp = timestamp;
    }

    public Long getTimestampMin() {
        return epochBase;
    }

    public Long getTimestampMax() {
        return epochBase + full();
    }

    public Instant toInstant(Long epoch) {
        return epochGenerator.epochToInstant(epoch);
    }

    public Long toEpoch(Instant instant) {
        return epochGenerator.instantToEpoch(instant);
    }

    @Override
    public Long getField(Entire entire) {
        Long ts = super.getField(entire);
        return ts + epochBase;
    }

    @Override
    public Entire putField(Entire entire, Long value) {
        Long ts = value - epochBase;
        return super.putField(entire, ts);
    }
}
