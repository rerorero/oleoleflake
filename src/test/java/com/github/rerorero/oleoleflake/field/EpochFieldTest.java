package com.github.rerorero.oleoleflake.field;

import com.github.rerorero.oleoleflake.bitset.LongCodec;
import com.github.rerorero.oleoleflake.epoch.EpochGenerator;
import org.junit.Test;

import java.time.Instant;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;

public class EpochFieldTest {

    long now = ZonedDateTime.now().toEpochSecond();
    EpochGenerator<Long> mockedEpochGenerator = new EpochGenerator<Long>() {
        @Override
        public Instant timeGen() {
            return Instant.ofEpochSecond(now);
        }

        @Override
        public Long instantToEpoch(Instant instant) {
            return EpochGenerator.currentTimeSecondsGenerator.instantToEpoch(instant);
        }

        @Override
        public Instant epochToInstant(Long epoch) {
            return EpochGenerator.currentTimeSecondsGenerator.epochToInstant(epoch);
        }
    };

    @Test
    public void timestampTest() {
        EpochField sut1 = new EpochField(4, 4, 8, LongCodec.singleton, now - 4, mockedEpochGenerator, false);
        assertEquals(Long.valueOf(now), sut1.currentTimestamp());
        assertEquals(4L, sut1.putField(0L, Long.valueOf(now)));
        assertEquals(8L, sut1.putField(0L, Long.valueOf(now+4)));
        assertEquals(Long.valueOf(now-4), sut1.getField(0L));
        assertEquals(Long.valueOf(now+4), sut1.getField(8L));

        assertEquals(Long.valueOf(now-4), sut1.getTimestampMin());
        assertEquals(Long.valueOf(now+11), sut1.getTimestampMax());
    }
}
