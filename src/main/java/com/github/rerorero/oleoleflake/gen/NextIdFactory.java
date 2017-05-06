package com.github.rerorero.oleoleflake.gen;

import com.github.rerorero.oleoleflake.field.TimestampField;

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
                if (!idGen.timestampField.isPresent()) {
                    nextSeq = seq.nextSequence();
                } else {
                    TimestampField<Entire> tsField = idGen.timestampField.get();
                    Long timestamp = tsField.currentTimestamp();
                    Long lastTimestamp = tsField.lastTimestamp();
                    if (tsField.fieldComparator().compare(timestamp, lastTimestamp) == 0) {
                        if (seq.reachedLimit()) {
                            timestamp = blockTillNext(tsField);
                            nextSeq = seq.resetSequence();
                        } else {
                            nextSeq = seq.nextSequence();
                        }
                    } else {
                        nextSeq = seq.resetSequence();
                    }
                    // timestamp field
                    entire = tsField.putField(entire, timestamp);
                    tsField.commitLastTimestamp(timestamp);
                }

                // sequence field
                entire = seq.putField(entire, nextSeq);
            });

            if (!idGen.sequenceField.isPresent()) {
                idGen.timestampField.ifPresent(tsField -> {
                    Long timestamp = tsField.currentTimestamp();
                    entire = tsField.putField(entire, timestamp);
                    tsField.commitLastTimestamp(timestamp);
                });
            }
        }

        return entire;
    }

    private Long blockTillNext(TimestampField<Entire> tsField) {
        Long timestamp = tsField.currentTimestamp();
        Long lastTimestamp = tsField.lastTimestamp();
        while(tsField.fieldComparator().compare(timestamp, lastTimestamp) <= 0) {
            timestamp = tsField.currentTimestamp();
        }
        return timestamp;
    }
}
