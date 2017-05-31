package com.github.rerorero.oleoleflake.gen;

import com.github.rerorero.oleoleflake.OleOleFlakeException;
import com.github.rerorero.oleoleflake.bitset.BitSetCodec;
import com.github.rerorero.oleoleflake.field.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IdGen<Entire, Seq> {

    final protected BitSetCodec<Entire> entireCodec;
    final protected Map<String, ConstantField<Entire, ?>> constantFields;
    final protected Map<String, NamedField<Entire, ?>> bindableFields;
    final protected Optional<ISequentialField<Entire, Seq>> sequenceField;
    final protected Optional<TimestampField<Entire>> timestampField;
    final protected List<FieldBase> unusedFields;
    final protected int entireBitLen;

    public IdGen(
            BitSetCodec<Entire> entireCodec,
            List<ConstantField<Entire, ?>> constantFields,
            List<NamedField<Entire, ?>> bindableFields,
            Optional<ISequentialField<Entire, Seq>> sequence,
            Optional<TimestampField<Entire>> timestamp,
            List<FieldBase> unusedFields,
            int entireBitLen
    ) {
        this.entireCodec = entireCodec;
        this.constantFields = constantFields.stream().collect(Collectors.toMap(ConstantField::getName, Function.identity()));
        this.bindableFields = bindableFields.stream().collect(Collectors.toMap(NamedField::getName, Function.identity()));
        this.sequenceField = sequence;
        this.timestampField = timestamp;
        this.unusedFields = unusedFields;
        this.entireBitLen = entireBitLen;
    }

    public int getIdBitLen() {
        return this.entireBitLen;
    }

    public List<IField> allFields() {
        ArrayList<IField> all = new ArrayList<>();
        all.addAll(constantFields.values());
        all.addAll(bindableFields.values());
        sequenceField.ifPresent(f -> all.add(f));
        timestampField.ifPresent(f -> all.add(f));
        all.addAll(unusedFields);
        return all;
    }

    public void validate() {
        List<IField> all = allFields();
        all.forEach(f -> f.validate());
    }

    public boolean hasSequenceField() {
        return sequenceField.isPresent();
    }

    public boolean hasTimestampField() {
        return timestampField.isPresent();
    }

    public SnapshotIdFactory<Entire, Seq> snapshot() {
        return newSnapshotIdFactory();
    }

    public NextIdFactory<Entire, Seq> next() {
        return newNextIdFactory();
    }

    public Instant parseTimestamp(Entire entire) {
        return timestampField.map(ts -> ts.toInstant(ts.getField(entire)))
                .orElseThrow(() -> new OleOleFlakeException("There is no timestamp field."));
    }

    public Seq parseSequence(Entire entire) {
        return sequenceField.map(seq -> seq.getField(entire))
                .orElseThrow(() -> new OleOleFlakeException("There is no sequence field."));
    }

    public <T> T parseConstant(String name, Entire entire) {
        if (!constantFields.keySet().contains(name))
            throw new OleOleFlakeException("No such constant field: " + name);
        ConstantField<Entire, T> field = (ConstantField<Entire, T>) constantFields.get(name);
        return field.getField(entire);
    }

    public <T> T parseBindable(String name, Entire entire) {
        if (!bindableFields.keySet().contains(name))
            throw new OleOleFlakeException("No such bindable field: " + name);
        NamedField<Entire, T> field = (NamedField<Entire, T>) bindableFields.get(name);
        return field.getField(entire);
    }

    protected NextIdFactory<Entire, Seq> newNextIdFactory() {
        return new NextIdFactory<Entire, Seq>(this);
    }

    protected SnapshotIdFactory<Entire, Seq> newSnapshotIdFactory() {
        return new SnapshotIdFactory<Entire, Seq>(this);
    }
}
