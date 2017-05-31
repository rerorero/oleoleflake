package com.github.rerorero.oleoleflake.gen.idbytes;

import com.github.rerorero.oleoleflake.util.MockedTimestampGenerator;
import org.junit.Test;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class IdBytesGenBuilderTest {
    Instant epochBase = Instant.parse("2015-10-10T15:15:00.00Z");

    @Test
    public void constantFieldBuilderTest() {
        IdBytesGen gen = IdBytesGen.builder(11)
                .nextBit(8).unusedField()
                .nextBit(32).constantField().name("constant1").value(new byte[] {(byte)0xfa, (byte)0x59, (byte)0xde, (byte)0xad})
                .nextBit(12).unusedField()
                .nextBit(32).constantStringField().name("constant2").value("ab")
                .nextBit(4).constantLongField().name("constant3").value(3L)
                .build();
        byte[] actual1 = gen.next().id();
        assertEquals(BigInteger.valueOf(0L), new BigInteger(Arrays.copyOfRange(actual1, 0, 1)));
        assertArrayEquals(new byte[]{(byte)0xfa, (byte)0x59, (byte)0xde, (byte)0xad}, Arrays.copyOfRange(actual1, 1, 5));
        assertEquals("ab", gen.parseConstant("constant2", actual1));
        assertEquals(Long.valueOf(3L), gen.parseConstant("constant3", actual1));
    }

    @Test
    public void bindableFieldBuilterTest() {
        IdBytesGen gen = IdBytesGen.builder(11)
                .nextBit(40).bindableStringField().name("bindable1")
                .nextBit(4).unusedField()
                .nextBit(30).bindableLongField().name("bindable2")
                .nextBit(14).bindableField().name("bindable3").flip()
                .build();
        byte[] actual1 = gen.next()
                .bind("bindable1", "cd")
                .bind("bindable2", 1234L)
                .bind("bindable3", new byte[]{(byte)0x0f, (byte)0xfa})
                .id();
        assertArrayEquals(new byte[]{0,0,0,(byte)0x63,(byte)0x64}, Arrays.copyOfRange(actual1, 0, 5));
        assertEquals("cd", gen.parseBindable("bindable1", actual1));
        assertEquals(Long.valueOf(1234L), gen.parseBindable("bindable2", actual1));
        assertArrayEquals(new byte[]{(byte)0x0f,(byte)0xfa}, gen.parseBindable("bindable3", actual1));
    }

    @Test
    public void timestampFieldBuilderTest() {
        MockedTimestampGenerator epoch = new MockedTimestampGenerator(epochBase);
        IdBytesGen gen = IdBytesGen.builder(11)
                .nextBit(40).unusedField()
                .nextBit(40).timestampField().withTimestampGenerator(epoch).startAt(epochBase)
                .nextBit(8).unusedField()
                .build();
        assertArrayEquals(new byte[]{0,0,0,0,0}, Arrays.copyOfRange(gen.next().id(), 5, 10));

        epoch.timestamp = epoch.timestamp + 1;
        assertArrayEquals(new byte[]{0,0,0,0,1}, Arrays.copyOfRange(gen.next().id(), 5, 10));

        byte[] actual3 = gen.snapshot().putTimestamp(epochBase.plusSeconds(11)).id();
        assertArrayEquals(new byte[]{0,0,0,0,11}, Arrays.copyOfRange(actual3, 5, 10));
        assertEquals(epochBase.plusSeconds(11), gen.parseTimestamp(actual3));
    }

    @Test
    public void sequenceFieldBuilderTest() {
        IdBytesGen gen = IdBytesGen.builder(11)
                .nextBit(16).unusedField()
                .nextBit(56).sequenceField().startAt(0b1000L)
                .nextBit(16).unusedField()
                .build();
        assertArrayEquals(new byte[]{0,0,0,0,0,0,3}, Arrays.copyOfRange(gen.snapshot().putSequence(0b11L).id(), 2, 9));
        assertArrayEquals(new byte[]{0,0,0,0,0,0,9}, Arrays.copyOfRange(gen.next().id(), 2, 9));
        assertArrayEquals(new byte[]{0,0,0,0,0,0,10}, Arrays.copyOfRange(gen.next().id(), 2, 9));
        assertEquals(Long.valueOf(11L), gen.parseSequence(gen.next().id()));
    }
}
