package com.github.rerorero.oleoleflake.gen;

import com.github.rerorero.oleoleflake.field.EpochField;

public class NextIdFactory<Entire, Seq> extends BindableIdFactory<Entire, Seq, NextIdFactory<Entire, Seq>> {

    public NextIdFactory(IdGen<Entire, Seq> idGen) {
        super(idGen);
    }

    @Override
    public Entire id() {
        // constant fields
        idGen.constantFields.forEach((name, field) -> {
            entire = field.putValueIntoField(entire);
        });

        synchronized (idGen) {
            idGen.sequenceField.ifPresent(seq -> {
                Seq nextSeq;
                if (!idGen.epochField.isPresent()) {
                    nextSeq = seq.nextSequence();
                } else {
                    EpochField<Entire> epoch = idGen.epochField.get();
                    Long timestamp = epoch.currentTimestamp();
                    Long lastTimestamp = epoch.lastTimestamp();
                    if (epoch.fieldComparator().compare(timestamp, lastTimestamp) == 0) {
                        if (seq.reachedLimit()) {
                            timestamp = blockTillNext(epoch);
                            nextSeq = seq.resetSequence();
                        } else {
                            nextSeq = seq.nextSequence();
                        }
                    } else {
                        nextSeq = seq.resetSequence();
                    }
                    // epoch field
                    entire = epoch.putField(entire, timestamp);
                    epoch.commitLastTimestamp(timestamp);
                }

                // sequence field
                entire = seq.putField(entire, nextSeq);
            });

            if (!idGen.sequenceField.isPresent()) {
                idGen.epochField.ifPresent(epoch -> {
                    Long timestamp = epoch.currentTimestamp();
                    entire = epoch.putField(entire, timestamp);
                    epoch.commitLastTimestamp(timestamp);
                });
            }
        }

        return entire;
    }

    private Long blockTillNext(EpochField<Entire> epoch) {
        Long timestamp = epoch.currentTimestamp();
        Long lastTimestamp = epoch.lastTimestamp();
        while(epoch.fieldComparator().compare(timestamp, lastTimestamp) <= 0) {
            timestamp = epoch.currentTimestamp();
        }
        return timestamp;
    }
}
