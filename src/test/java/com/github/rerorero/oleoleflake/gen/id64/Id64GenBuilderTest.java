package com.github.rerorero.oleoleflake.gen.id64;

import com.github.rerorero.oleoleflake.gen.id64.Id64Gen;
import com.github.rerorero.oleoleflake.util.MockedEpochGenerator;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

public class Id64GenBuilderTest {

    Instant epochBase = Instant.parse("2015-10-10T15:15:00.00Z");

    @Test
    public void constantFieldBuilderTest() {
        Id64Gen gen = Id64Gen.builder()
                .nextBit(20).unusedField()
                .nextBit(12).constantField().name("constant1").value(0b100000101011L)
                .nextBit(10).unusedField()
                .nextBit(20).constantField().name("contsant2").value(0b01001010111100000001L).flip()
                .nextBit(2).constantField().name("contsant3").value(0b11)
                .build();
        Long expect = Long.valueOf(0b0000000000000000000010000010101100000000001011010100001111111011L);
        assertEquals(expect, gen.next().id());
        assertEquals(expect, gen.next().id());
        assertEquals(expect, gen.snapshot().id());
    }

    @Test
    public void bindableFieldBuilterTest() {
        Id64Gen gen = Id64Gen.builder()
                .nextBit(20).unusedField()
                .nextBit(12).bindableField().name("bindable1")
                .nextBit(10).unusedField()
                .nextBit(20).bindableField().name("bindable2").flip()
                .nextBit(2).bindableField().name("bindable3")
                .build();
        Long expect = Long.valueOf(0b0000000000000000000010000010101100000000001011010100001111111011L);
        assertEquals(expect, gen.next()
                .bind("bindable1", Long.valueOf(0b100000101011L))
                .bind("bindable2", Long.valueOf(0b01001010111100000001L))
                .bind("bindable3", Long.valueOf(0b11))
                .id());
        assertEquals(expect, gen.snapshot()
                .bind("bindable1", Long.valueOf(0b100000101011L))
                .bind("bindable2", Long.valueOf(0b01001010111100000001L))
                .bind("bindable3", Long.valueOf(0b11))
                .id());
    }

    @Test
    public void epochFieldBuilderTest() {
        MockedEpochGenerator epoch = new MockedEpochGenerator(epochBase);
        Id64Gen gen = Id64Gen.builder()
                .nextBit(20).unusedField()
                .nextBit(42).epochField().withEpochGenerator(epoch).startAt(epochBase)
                .nextBit(2).unusedField()
                .build();
        assertEquals(Long.valueOf(0b1000L), gen.snapshot().putEpoch(epochBase.plusSeconds(2)).id());
        assertEquals(Long.valueOf(0b0L), gen.next().id());
        epoch.timestamp = epoch.timestamp + 1;
        assertEquals(Long.valueOf(0b100L), gen.next().id());
        assertEquals(Long.valueOf(0b100L), gen.next().id());

        epoch.timestamp = epochBase.getEpochSecond();
        Id64Gen flipgen = Id64Gen.builder()
                .nextBit(20).unusedField()
                .nextBit(42).epochField().withEpochGenerator(epoch).startAt(epochBase).flip()
                .nextBit(2).unusedField()
                .build();
        assertEquals(Long.valueOf(0b11111111111111111111111111111111111111110100L), flipgen.snapshot().putEpoch(epochBase.plusSeconds(2)).id());
        epoch.timestamp = epoch.timestamp + 1;
        assertEquals(Long.valueOf(0b11111111111111111111111111111111111111111000L), flipgen.next().id());
    }

    @Test
    public void sequenceFieldBuilderTest() {
        Id64Gen gen = Id64Gen.builder()
                .nextBit(30).unusedField()
                .nextBit(32).sequenceField().startAt(0b1000L)
                .nextBit(2).unusedField()
                .build();
        assertEquals(Long.valueOf(0b1100L), gen.snapshot().putSequence(0b11L).id());
        assertEquals(Long.valueOf(0b100100L), gen.next().id());
        assertEquals(Long.valueOf(0b101000L), gen.next().id());

        Id64Gen flipgen = Id64Gen.builder()
                .nextBit(30).unusedField()
                .nextBit(32).sequenceField().startAt(0b1000L).flip()
                .nextBit(2).unusedField()
                .build();
        assertEquals(Long.valueOf(0b1111111111111111111111111111110000L), flipgen.snapshot().putSequence(0b11L).id());
        assertEquals(Long.valueOf(0b1111111111111111111111111111011000L), flipgen.next().id());
        assertEquals(Long.valueOf(0b1111111111111111111111111111010100L), flipgen.next().id());
    }
}
