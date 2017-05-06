package com.github.rerorero.oleoleflake.gen;

import com.github.rerorero.oleoleflake.OleOleFlakeException;
import com.github.rerorero.oleoleflake.field.ConstantField;
import com.github.rerorero.oleoleflake.field.ISequentialField;
import com.github.rerorero.oleoleflake.field.TimestampField;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SnapshotIdFactory<Entire, Seq> extends BindableIdFactory<Entire, Seq, SnapshotIdFactory<Entire, Seq>> {
    private Instant time = null;
    private Seq sequence = null;
    private Map<String, ConstantField<Entire, ?>> modifiedConstantFields = new HashMap<>();

    public SnapshotIdFactory(IdGen<Entire, Seq> idGen) {
        super(idGen);
    }

    private <T> T withTimestampField(final Function<TimestampField<Entire>, T> f) {
        return idGen.timestampField.map(tsField -> f.apply(tsField))
                .orElseThrow(() -> new OleOleFlakeException("No timestamp fields available."));
    }

    public SnapshotIdFactory putTimestamp(Instant time) {
        if (this.time != null)
            throw new OleOleFlakeException("timestamp has already been set.");
        return withTimestampField(ts -> {
            this.time = time;
            return this;
        });
    }

    public SnapshotIdFactory putTimestampMin() {
        return withTimestampField(ts -> putTimestamp(ts.toInstant(ts.getTimestampMin())));
    }

    public SnapshotIdFactory putTimestampMax() {
        return withTimestampField(ts -> putTimestamp(ts.toInstant(ts.getTimestampMax())));
    }

    private <T> T withSequenceField(final Function<ISequentialField<Entire, Seq>, T> f) {
        return idGen.sequenceField.map(field -> f.apply(field))
                .orElseThrow(() -> new OleOleFlakeException("No sequence fields available."));
    }

    public SnapshotIdFactory putSequence(Seq seq) {
        if (this.sequence != null)
            throw new OleOleFlakeException("Sequence has already been set.");
        return withSequenceField(f -> {
            this.sequence = seq;
            return this;
        });
    }

    public SnapshotIdFactory putSequenceMin() {
        return withSequenceField(field -> putSequence(field.zero()));
    }

    public SnapshotIdFactory putSequenceInitialValue() {
        return withSequenceField(field -> putSequence(field.initialValue()));
    }

    public SnapshotIdFactory putSequenceMax() {
        return withSequenceField(field -> putSequence(field.full()));
    }

    public <T> SnapshotIdFactory putConstantValue(String name, T value) {
        ConstantField<Entire, ?> field = idGen.constantFields.get(name);
        if (field == null)
            throw new OleOleFlakeException("No such field: " + name);
        if (modifiedConstantFields.keySet().contains(name))
            throw new OleOleFlakeException(String.format("Constant value (%s) has already been set.", name));
        ConstantField<Entire, T> fieldT = (ConstantField<Entire, T>) field.clone();
        fieldT.setConstantValue(value);
        modifiedConstantFields.put(name, fieldT);
        return this;
    }

    @Override
    public Entire id() {
        // sequence field
        idGen.sequenceField.ifPresent(field -> {
            if (sequence == null)
                throw new OleOleFlakeException("No sequence is set. You should call putSequence()."); entire = field.putField(entire, sequence);
        });

        // timestamp fieild
        idGen.timestampField.ifPresent(field -> {
            if (time == null)
                throw new OleOleFlakeException("No timestamp is set. You should call putTimestamp().");
            entire = field.putField(entire, field.toTimestamp(time));
        });

        // constant fileds
        idGen.constantFields.forEach((name, field) -> {
            ConstantField<Entire, ?> modified = modifiedConstantFields.get(name);
            if (modified != null) {
                entire = modified.putValueIntoField(entire);
            } else {
                entire = field.putValueIntoField(entire);
            }
        });
        return entire;
    }
}
