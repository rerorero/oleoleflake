package com.github.rerorero.oleoleflake.gen.id64;

import com.github.rerorero.oleoleflake.OleOleFlakeException;
import com.github.rerorero.oleoleflake.bitset.BitSetCodec;
import com.github.rerorero.oleoleflake.bitset.LongCodec;
import com.github.rerorero.oleoleflake.epoch.TimestampGenerator;
import com.github.rerorero.oleoleflake.field.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

public class Id64GenBuilder {

    private static final int ENTIRE_SIZE = 64;
    private static final BitSetCodec<Long> entireCodec = LongCodec.singleton;

    private int bitPointer = 0;
    private ArrayList<ConstantField<Long, ?>> constantFields = new ArrayList<>();
    private ArrayList<NamedField<Long, ?>> bindableFields = new ArrayList<>();
    private ArrayList<FieldBase> unusedFields = new ArrayList<>();
    private Optional<ISequentialField<Long, Long>> sequence = Optional.empty();
    private Optional<TimestampField<Long>> timestamp = Optional.empty();

    public abstract class FieldBuilder<Builder extends FieldBuilder<Builder>> {
        protected final int start;
        protected final int size;
        protected boolean invert = false;

        public FieldBuilder(int start, int size) {
            this.start = start;
            this.size = size;
        }

        public Builder invert() {
            invert = true;
            return (Builder)this;
        }

        public Builder flip() {
            return invert();
        }

        abstract protected void setup();

        public FieldBuilderSelector nextBit(int size) {
            setup();
            return Id64GenBuilder.this.nextBit(size);
        }

        public Id64Gen build() {
            setup();
            return Id64GenBuilder.this.build();
        }
    }

    public class FieldBuilderSelector extends FieldBuilder<FieldBuilderSelector> {
        public FieldBuilderSelector(int start, int size) {
            super(start, size);
        }

        public ConstantFieldBuilder constantField() {
            return new ConstantFieldBuilder(this);
        }

        public TimestampFieldBuilder timestampField() {
            return new TimestampFieldBuilder(this);
        }

        public SequenceFieldBuilder sequenceField() {
            return new SequenceFieldBuilder(this);
        }

        public BindableFieldBuilder bindableField() {
            return new BindableFieldBuilder(this);
        }

        public Id64GenBuilder unusedField() {
            FieldBase field = new FieldBase(start, size, ENTIRE_SIZE);
            unusedFields.add(field);
            return Id64GenBuilder.this;
        }

        @Override
        protected void setup() {
            // nop
        }
    }

    public class ConstantFieldBuilder extends FieldBuilder<ConstantFieldBuilder> {

        private String _name = null;
        private Long _value = null;

        public ConstantFieldBuilder(FieldBuilder fb) {
            super(fb.start, fb.size);
        }

        public ConstantFieldBuilder name(String n) {
            if (_name != null)
                throw new OleOleFlakeException("name() was already set.");
            _name = n;
            return this;
        }

        public ConstantFieldBuilder value(long v) {
            if (_value != null)
                throw new OleOleFlakeException("value() was already set.");
            _value = v;
            return this;
        }

        @Override
        protected void setup() {
            if (_value == null)
                throw new OleOleFlakeException("Constant value field requires value().");
            if (constantFields.stream().anyMatch(f -> f.getName().equals(_name)))
                throw new OleOleFlakeException("A duplicate field name exists: " + _name);
            if (_name == null)
                _name = String.format("const-%d-%d", start, size);

            ConstantField<Long, Long> field = new ConstantField<>(
                    start,
                    size,
                    ENTIRE_SIZE,
                    entireCodec,
                    LongCodec.singleton,
                    _name,
                    _value,
                    invert
            );
            constantFields.add(field);
        }
    }

    public class BindableFieldBuilder extends FieldBuilder<BindableFieldBuilder> {

        private String _name = null;

        public BindableFieldBuilder(FieldBuilder fb) {
            super(fb.start, fb.size);
        }

        public BindableFieldBuilder name(String n) {
            if (_name != null)
                throw new OleOleFlakeException("name() was already set.");
            _name = n;
            return this;
        }

        @Override
        protected void setup() {
            if (bindableFields.stream().anyMatch(f -> f.getName().equals(_name)))
                throw new OleOleFlakeException("A duplicate field name exists: " + _name);
            if (_name == null)
                _name = String.format("bindable-%d-%d", start, size);

            NamedField<Long, Long> field = new NamedField<>(
                    start,
                    size,
                    ENTIRE_SIZE,
                    entireCodec,
                    LongCodec.singleton,
                    _name,
                    invert
            );
            bindableFields.add(field);
        }
    }

    public class TimestampFieldBuilder extends FieldBuilder<TimestampFieldBuilder> {
        private TimestampGenerator<Long> timestampGen = null;
        private Instant origin = null;

        public TimestampFieldBuilder(FieldBuilder fb) {
            super(fb.start, fb.size);
        }

        public TimestampFieldBuilder tickPerMillisec() {
            return withTimestampGenerator(TimestampGenerator.currentTimeMillisGenerator);
        }

        public TimestampFieldBuilder tickPerSecond() {
            return withTimestampGenerator(TimestampGenerator.currentTimeMillisGenerator);
        }

        public TimestampFieldBuilder withTimestampGenerator(TimestampGenerator<Long> generator) {
            if (timestampGen != null)
                throw new OleOleFlakeException("Timestamp generator was already set.");
            timestampGen = generator;
            return this;
        }

        public TimestampFieldBuilder startAt(Instant instant) {
            if (origin != null)
                throw new OleOleFlakeException("startAt() was already set.");
            origin = instant;
            return this;
        }

        @Override
        protected void setup() {
            if (timestamp.isPresent())
                throw new OleOleFlakeException("A duplicate timestamp field exists. You can only specify one timestamp.");
            if (origin == null)
                throw new OleOleFlakeException("Timestamp field must have an startAt().");
            if (timestampGen == null)
                throw new OleOleFlakeException("Timestamp field must have an timestamp generator.");

            timestamp = Optional.of(
                    new TimestampField<Long>(
                            start,
                            size,
                            ENTIRE_SIZE,
                            entireCodec,
                            timestampGen.instantToTimestamp(origin),
                            timestampGen,
                            invert
                    ));
        }
    }

    public class SequenceFieldBuilder extends FieldBuilder<SequenceFieldBuilder> {
        private Long origin = null;
        private LongSequentialField.LongSequencer<Long> sequencer = null;

        public SequenceFieldBuilder(FieldBuilder fb) {
            super(fb.start, fb.size);
        }

        public SequenceFieldBuilder startAt(long o) {
            if (origin != null)
                throw new OleOleFlakeException("startAt() was already set.");
            origin = o;
            return this;
        }

        public SequenceFieldBuilder incremental() {
            return sequencer(LongSequentialField.incrementalSequencer());
        }

        public SequenceFieldBuilder sequencer(LongSequentialField.LongSequencer<Long> sequencer) {
            if (sequencer == null)
                throw new OleOleFlakeException("sequencer was already set.");
            this.sequencer = sequencer;
            return this;
        }

        @Override
        protected void setup() {
            if (sequence.isPresent())
                throw new OleOleFlakeException("A duplicate sequence field exists. You can only specify one sequence.");
            if (origin == null)
                origin = 0L;
            if (sequencer == null)
                this.incremental();
            sequence = Optional.of(new LongSequentialField<Long>(
                    start,
                    size,
                    ENTIRE_SIZE,
                    entireCodec,
                    origin,
                    invert,
                    this.sequencer
            ));
        }
    }

    public FieldBuilderSelector nextBit(int size) {
        if (bitPointer + size > ENTIRE_SIZE) {
            throw new OleOleFlakeException(String.format("Field size(%d) is too large.",size));
        }
        FieldBuilderSelector fb =  new FieldBuilderSelector(bitPointer, size);
        bitPointer += size;
        return fb;
    }

    public Id64Gen build() {
        if (bitPointer < ENTIRE_SIZE)
            throw new OleOleFlakeException(String.format("The field size is too small. Please set it to be %d bit. For unused fields, set unusedField().", ENTIRE_SIZE));
        if (bitPointer > ENTIRE_SIZE)
            throw new OleOleFlakeException(String.format("The field size is too large. Please set it to be %d bit. For unused fields, set unusedField().", ENTIRE_SIZE));

        Id64Gen gen = new Id64Gen(entireCodec, constantFields, bindableFields, sequence, timestamp, unusedFields);
        gen.validate();
        return gen;
    }
}
