package com.github.rerorero.oleoleflake.gen;

import com.github.rerorero.oleoleflake.bitset.BitSetCodec;
import com.github.rerorero.oleoleflake.field.EpochField;
import com.github.rerorero.oleoleflake.field.SequentialField;

import java.util.BitSet;
import java.util.Optional;

public class IdGen<Entire, Field, Seq, Epoch> {

    private BitSetCodec<Entire> entireCodec;
    private Optional<SequentialField<Entire, Seq>> sequenceField;
    private Optional<EpochField<Entire, Epoch>> epochField;

    public IdGen(BitSetCodec<Entire> entireCodec, Optional<SequentialField<Entire, Seq>> sequence, Optional<EpochField<Entire, Epoch>> epoch) {
        this.entireCodec = entireCodec;
        this.sequenceField = sequence;
        this.epochField = epoch;
    }

    public Entire nextId() {
        Entire entire = entireCodec.toValue(new BitSet());
        synchronized (this) {
            sequenceField.ifPresent(seq -> {
                Seq sequence = epochField.map( epoch -> {
                    Epoch timestamp = epoch.currentTimestamp();
                    Epoch lastTimestamp = epoch.lastTimestamp();
                    Seq nextSeq;
                    if (epoch.comparator().compare(timestamp, lastTimestamp) == 0) {
                        if (seq.reachedLimit()) {
                            timestamp = blockTillNext(epoch);
                            nextSeq = seq.resetSequence();
                        } else {
                            nextSeq = seq.nextSequence();
                        }
                    } else {
                        nextSeq = seq.resetSequence();
                    }
                    epoch.commitLastTimestamp(timestamp);
                    return nextSeq;
                }).orElse(seq.nextSequence());

                seq.putField(entire, sequence);
            });
        }
        return entire;
    }

    private Epoch blockTillNext(EpochField<Entire, Epoch> epoch) {
        Epoch timestamp = epoch.currentTimestamp();
        Epoch lastTimestamp = epoch.lastTimestamp();
        while(epoch.comparator().compare(timestamp, lastTimestamp) <= 0) {
            timestamp = epoch.currentTimestamp();
        }
        return timestamp;
    }
}
