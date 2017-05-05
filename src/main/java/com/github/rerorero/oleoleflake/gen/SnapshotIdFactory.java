package com.github.rerorero.oleoleflake.gen;

import com.github.rerorero.oleoleflake.OleOleFlakeException;
import com.github.rerorero.oleoleflake.field.ConstantField;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class SnapshotIdFactory<Entire, Seq> extends BindableIdFactory<Entire, Seq, SnapshotIdFactory<Entire, Seq>> {
    private Instant time = null;
    private Seq sequence = null;
    private Map<String, ConstantField<Entire, ?>> modifiedConstantFields = new HashMap<>();

    public SnapshotIdFactory(IdGen<Entire, Seq> idGen) {
        super(idGen);
    }

    public SnapshotIdFactory putEpoch(Instant time) {
        if (!idGen.hasEpochField())
            throw new OleOleFlakeException("No epoch fields available.");
        if (this.time != null)
            throw new OleOleFlakeException("putEpoch() has already set.");
        this.time = time;
        return this;
    }

    public SnapshotIdFactory putSequence(Seq seq) {
        if (!idGen.hasSequenceField())
            throw new OleOleFlakeException("No sequence fields available.");
        if (this.sequence != null)
            throw new OleOleFlakeException("putSequence() has already set.");
        this.sequence = seq;
        return this;
    }

    public <T> SnapshotIdFactory putConstantValue(String name, T value) {
        ConstantField<Entire, ?> field = idGen.constantFields.get(name);
        if (field == null)
            throw new OleOleFlakeException("No such field: " + name);
        if (modifiedConstantFields.keySet().contains(name))
            throw new OleOleFlakeException(String.format("putConstantValue(%s) has already set.", name));
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

        // epoch fieild
        idGen.epochField.ifPresent(field -> {
            if (time == null)
                throw new OleOleFlakeException("No epoch is set. You should call putEpoch().");
            entire = field.putField(entire, field.toEpoch(time));
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
