package com.github.rerorero.oleoleflake.field;

import com.github.rerorero.oleoleflake.bitset.BitSetCodec;
import com.github.rerorero.oleoleflake.epoch.TimestampGenerator;

import java.time.Instant;

/**
 * @note Warning! Thread unsafety!
 */
public class TimestampField<Entire> extends LongField<Entire> {

    private final long epochBase;
    private final TimestampGenerator<Long> timestampGenerator;
    private long lastTimestamp;

    public TimestampField(
            int start,
            int size,
            int entireSize,
            BitSetCodec<Entire> entireCodec,
            long origin,
            TimestampGenerator<Long> epochGen,
            boolean invert
    ) {
        super(start, size, entireSize, entireCodec, invert);
        this.epochBase = origin;
        this.lastTimestamp = origin;
        this.timestampGenerator = epochGen;
    }

    public Long currentTimestamp() {
        long timestamp = timestampGenerator.timeGenT();
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
        return timestampGenerator.timestampToInstant(epoch);
    }

    public Long toTimestamp(Instant instant) {
        return timestampGenerator.instantToTimestamp(instant);
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
