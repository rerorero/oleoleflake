package com.github.rerorero.oleoleflake.gen;

import com.github.rerorero.oleoleflake.gen.id64.Id64Gen;
import com.github.rerorero.oleoleflake.util.Executor;
import com.github.rerorero.oleoleflake.util.MockedTimestampGenerator;
import org.junit.Test;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;

public class IdGenTest {

    Instant epochBase = Instant.parse("2015-10-10T15:15:00.00Z");
    ExecutorService executor = Executor.gen();

    @Test
    public void nextIdTest() throws ExecutionException, InterruptedException {
        MockedTimestampGenerator epoch = new MockedTimestampGenerator(epochBase);
        Id64Gen gen = Id64Gen.builder()
                .nextBit(30).unusedField()
                .nextBit(30).timestampField().withTimestampGenerator(epoch).startAt(epochBase)
                .nextBit(3).sequenceField().startAt(0b001L)
                .nextBit(1).unusedField()
                .build();
        epoch.timestamp = epoch.timestamp+1;
        assertEquals(Long.valueOf(0b10010L), gen.next().id());
        assertEquals(Long.valueOf(0b10100L), gen.next().id());
        for (int i=0; i<5; i++) {
            gen.next().id();
        }

        CompletableFuture<Long> f1 = CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();
            // block until next timestamp
            Long id = gen.next().id();
            assertTrue((System.currentTimeMillis() - start) >= 1000);
            return id;
        }, executor);

        CompletableFuture<?> f2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail();
            }
            epoch.timestamp = epoch.timestamp + 1;
            return null;
        }, executor);

        Long id = f1.get();
        assertEquals(Long.valueOf(0b100010L), id);
    }

    @Test
    public void parseTest() {
        MockedTimestampGenerator epoch = new MockedTimestampGenerator(epochBase);
        Id64Gen gen = Id64Gen.builder()
                .nextBit(4).unusedField()
                .nextBit(10).timestampField().withTimestampGenerator(epoch).startAt(epochBase)
                .nextBit(10).sequenceField().startAt(0b001L)
                .nextBit(10).constantField().name("constant1").value(0b010L)
                .nextBit(10).constantField().name("constant2").value(0b101L).flip()
                .nextBit(10).bindableField().name("bind1")
                .nextBit(10).bindableField().name("bind2").flip()
                .build();

        long id = 0b100000000101010000001111000000010100101010010001110001L;
        assertEquals(epochBase.plusSeconds(0b1000L), gen.parseTimestamp(id));
        assertEquals(Long.valueOf(0b10101L), gen.parseSequence(id));
        assertEquals(Long.valueOf(0b1111L), gen.parseConstant("constant1", id));
        assertEquals(Long.valueOf(0b1111111010L), gen.parseConstant("constant2", id));
        assertEquals(Long.valueOf(0b10101001L), gen.parseBindable("bind1", id));
        assertEquals(Long.valueOf(0b1110001110L), gen.parseBindable("bind2", id));
    }
}
