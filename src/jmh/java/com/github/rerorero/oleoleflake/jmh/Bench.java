package com.github.rerorero.oleoleflake.jmh;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.time.Instant;
import com.github.rerorero.oleoleflake.gen.id64.Id64Gen;


@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class Bench {

    static final Id64Gen gen = Id64Gen.builder()
            .nextBit(1).unusedField()
            .nextBit(41).timestampField().tickPerMillisec().startAt(Instant.parse("2017-01-01T00:00:00.00Z"))
            .nextBit(5).constantField().name("data-center-id").value(1)
            .nextBit(5).constantField().name("worker-id").value(2)
            .nextBit(12).sequenceField()
            .build();

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Threads(4)
    public void nextId() {
        for (int i = 0; i < 1000; i ++) {
            gen.next().id();
        }
    }
}