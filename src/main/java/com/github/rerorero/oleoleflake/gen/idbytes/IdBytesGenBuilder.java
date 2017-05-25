package com.github.rerorero.oleoleflake.gen.idbytes;

import com.github.rerorero.oleoleflake.OleOleFlakeException;
import com.github.rerorero.oleoleflake.bitset.BitSetCodec;
import com.github.rerorero.oleoleflake.bitset.BytesCodec;
import com.github.rerorero.oleoleflake.epoch.TimestampGenerator;
import com.github.rerorero.oleoleflake.field.*;
import com.github.rerorero.oleoleflake.gen.AbstractIdGenBuilder;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

public class IdBytesGenBuilder extends AbstractIdGenBuilder {
    private static final BitSetCodec<byte[]> entireCodec = BytesCodec.singleton;

    private final int entireSize;
    private int bitPointer = 0;
    private ArrayList<ConstantField<byte[], ?>> constantFields = new ArrayList<>();
    private ArrayList<NamedField<byte[], ?>> bindableFields = new ArrayList<>();
    private ArrayList<FieldBase> unusedFields = new ArrayList<>();
    private Optional<ISequentialField<byte[], Long>> sequence = Optional.empty();
    private Optional<TimestampField<byte[]>> timestamp = Optional.empty();

    public IdBytesGenBuilder(int idLength) {
        this.entireSize = idLength;
    }

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
            return IdBytesGenBuilder.this.nextBit(size);
        }

        public IdBytesGen build() {
            setup();
            return IdBytesGenBuilder.this.build();
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

//        public BindableFieldBuilder bindableField() {
//            return new BindableFieldBuilder(this);
//        }

        public IdBytesGenBuilder unusedField() {
            FieldBase field = new FieldBase(start, size, entireSize);
            unusedFields.add(field);
            return IdBytesGenBuilder.this;
        }

        @Override
        protected void setup() {
            // nop
        }
    }

    public class ConstantFieldBuilder extends FieldBuilder<ConstantFieldBuilder> {

        private String _name = null;
        private byte[] _value = null;

        public ConstantFieldBuilder(FieldBuilder fb) {
            super(fb.start, fb.size);
        }

        public ConstantFieldBuilder name(String n) {
            if (_name != null)
                throw new OleOleFlakeException("name() was already set.");
            _name = n;
            return this;
        }

        public ConstantFieldBuilder value(byte[] v) {
            if (_value != null)
                throw new OleOleFlakeException("value() was already set.");
            _value = v;
            return this;
        }

        public ConstantFieldBuilder value(String v) {
            return value(v.getBytes());
        }

        public ConstantFieldBuilder value(String v, Charset charset) {
            return value(v.getBytes(charset));
        }

        @Override
        protected void setup() {
            if (_value == null)
                throw new OleOleFlakeException("Constant value field requires value().");
            if (constantFields.stream().anyMatch(f -> f.getName().equals(_name)))
                throw new OleOleFlakeException("A duplicate field name exists: " + _name);
            if (_value.length > size)
                throw new OleOleFlakeException(String.format("The size of a field '%s' is too long.", _name));
            if (_name == null)
                _name = String.format("const-%d-%d", start, size);

            byte[] fixedArray;
            if (_value.length < size) {
                byte[] padding = new byte[size -  _name.length()];
                fixedArray = new byte[size];
                System.arraycopy(_value,0,fixedArray,0, _value.length);
                System.arraycopy(padding,0,fixedArray,_value.length, padding.length);
            } else {
                fixedArray = _value;
            }

            ConstantField<byte[], byte[]> field = new ConstantField<>(
                    start,
                    size,
                    entireSize,
                    entireCodec,
                    BytesCodec.singleton,
                    _name,
                    fixedArray,
                    invert
            );

            constantFields.add(field);
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
                    new TimestampField<byte[]>(
                            start,
                            size,
                            entireSize,
                            entireCodec,
                            timestampGen.instantToTimestamp(origin),
                            timestampGen,
                            invert
                    ));
        }
    }

    public class SequenceFieldBuilder extends FieldBuilder<SequenceFieldBuilder> {
        private Long origin = null;
        private LongSequentialField.LongSequencer<byte[]> sequencer = null;

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

        public SequenceFieldBuilder sequencer(LongSequentialField.LongSequencer<byte[]> sequencer) {
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
            sequence = Optional.of(new LongSequentialField<byte[]>(
                    start,
                    size,
                    entireSize,
                    entireCodec,
                    origin,
                    invert,
                    this.sequencer
            ));
        }
    }

    public FieldBuilderSelector nextBit(int size) {
        if (bitPointer + size > entireSize) {
            throw new OleOleFlakeException(String.format("Field size(%d) is too large.",size));
        }
        FieldBuilderSelector fb =  new FieldBuilderSelector(bitPointer, size);
        bitPointer += size;
        return fb;
    }

    public IdBytesGen build() {
        if (bitPointer < entireSize)
            throw new OleOleFlakeException(String.format("The field size is too small. Please set it to be %d bit. For unused fields, set unusedField().", entireCodec));
        if (bitPointer > entireSize)
            throw new OleOleFlakeException(String.format("The field size is too large. Please set it to be %d bit. For unused fields, set unusedField().", entireCodec));

        IdBytesGen gen = new IdBytesGen(entireCodec, constantFields, bindableFields, sequence, timestamp, unusedFields);
        gen.validate();
        return gen;
    }

    private Byte[] toByteArray(byte[] bytes) {
        Byte[] boxed = new Byte[bytes.length];
        int i = 0;
        for(byte b: bytes) boxed[i++] = b;
        return boxed;
    }
}
