package com.github.rerorero.oleoleflake.gen.idbytes;

import com.github.rerorero.oleoleflake.OleOleFlakeException;
import com.github.rerorero.oleoleflake.bitset.BitSetCodec;
import com.github.rerorero.oleoleflake.bitset.BytesCodec;
import com.github.rerorero.oleoleflake.bitset.LongCodec;
import com.github.rerorero.oleoleflake.bitset.StringCodec;
import com.github.rerorero.oleoleflake.epoch.TimestampGenerator;
import com.github.rerorero.oleoleflake.field.*;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

public class IdBytesGenBuilder {
    private static final BitSetCodec<byte[]> entireCodec = BytesCodec.singleton;

    private final int entireSize;
    private int bitPointer = 0;
    private ArrayList<ConstantField<byte[], ?>> constantFields = new ArrayList<>();
    private ArrayList<NamedField<byte[], ?>> bindableFields = new ArrayList<>();
    private ArrayList<FieldBase> unusedFields = new ArrayList<>();
    private Optional<ISequentialField<byte[], Long>> sequence = Optional.empty();
    private Optional<TimestampField<byte[]>> timestamp = Optional.empty();

    public IdBytesGenBuilder(int byteLength) {
        // entireSize is a bit number.
        this.entireSize = byteLength * 8;
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

        public StringConstantFieldBuilder constantStringField() {
            return constantStringField(Charset.defaultCharset());
        }

        public StringConstantFieldBuilder constantStringField(Charset charset) {
            return new StringConstantFieldBuilder(this, charset);
        }

        public BytesConstantFieldBuilder constantField() {
            return new BytesConstantFieldBuilder(this);
        }

        public LongConstantFieldBuilder constantLongField() {
            return new LongConstantFieldBuilder(this);
        }

        public TimestampFieldBuilder timestampField() {
            return new TimestampFieldBuilder(this);
        }

        public SequenceFieldBuilder sequenceField() {
            return new SequenceFieldBuilder(this);
        }

        public StringBindableFieldBuilder bindableStringField(Charset charset) {
            return new StringBindableFieldBuilder(this, charset);
        }

        public StringBindableFieldBuilder bindableStringField() {
            return bindableStringField(Charset.defaultCharset());
        }

        public LongBindableFieldBuilder bindableLongField() {
            return new LongBindableFieldBuilder(this);
        }

        public BytesBindableFieldBuilder bindableField() {
            return new BytesBindableFieldBuilder(this);
        }

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

    public abstract class ConstantFieldBuilder<Builder extends FieldBuilder<Builder>, V> extends FieldBuilder<Builder> {

        protected String _name = null;
        protected V _value = null;

        public ConstantFieldBuilder(FieldBuilder fb) {
            super(fb.start, fb.size);
        }

        public Builder name(String n) {
            if (_name != null)
                throw new OleOleFlakeException("name() was already set.");
            _name = n;
            return (Builder) this;
        }

        public Builder value(V v) {
            if (_value != null)
                throw new OleOleFlakeException("value() was already set.");
            _value = v;
            return (Builder) this;
        }

        abstract protected ConstantField<byte[], V> newField();

        @Override
        protected void setup() {
            if (_value == null)
                throw new OleOleFlakeException("Constant value field requires value().");
            if (constantFields.stream().anyMatch(f -> f.getName().equals(_name)))
                throw new OleOleFlakeException("A duplicate field name exists: " + _name);
            if (_name == null)
                _name = String.format("const-%d-%d", start, size);

            ConstantField<byte[], V> field = newField();

            constantFields.add(field);
        }
    }

    public class BytesConstantFieldBuilder extends ConstantFieldBuilder<BytesConstantFieldBuilder, byte[]> {
        public BytesConstantFieldBuilder(FieldBuilder fb) {
            super(fb);
        }

        @Override
        protected ConstantField<byte[], byte[]> newField() {
            if (_value.length * 8 > size)
                throw new OleOleFlakeException(String.format("The size of a field '%s' is too long.", _name));

            return new ConstantField<>(
                    start,
                    size,
                    entireSize,
                    entireCodec,
                    BytesCodec.singleton,
                    _name,
                    _value,
                    invert
            );
        }
    }

    public class LongConstantFieldBuilder extends ConstantFieldBuilder<LongConstantFieldBuilder, Long> {
        public LongConstantFieldBuilder(FieldBuilder fb) {
            super(fb);
        }

        @Override
        protected ConstantField<byte[], Long> newField() {

            if (LongCodec.singleton.toBitSet(_value).length() > size)
                throw new OleOleFlakeException(String.format("The size of a field '%s' is too long.", _name));

            return new ConstantField<>(
                    start,
                    size,
                    entireSize,
                    entireCodec,
                    LongCodec.singleton,
                    _name,
                    _value,
                    invert
            );
        }
    }

    public class StringConstantFieldBuilder extends ConstantFieldBuilder<StringConstantFieldBuilder, String> {
        private final StringCodec codec;

        public StringConstantFieldBuilder(FieldBuilder fb, Charset charset) {
            super(fb);
            this.codec = new StringCodec(charset);
        }

        @Override
        protected ConstantField<byte[], String> newField() {
            if (codec.toBitSet(_value).length() > size)
                throw new OleOleFlakeException(String.format("The size of a field '%s' is too long.", _name));

            return new ConstantField<>(
                    start,
                    size,
                    entireSize,
                    entireCodec,
                    codec,
                    _name,
                    _value,
                    invert
            );
        }
    }
    public abstract class BindableFieldBuilder<Builder extends FieldBuilder<Builder>, V> extends FieldBuilder<Builder> {

        protected String _name = null;

        public BindableFieldBuilder(FieldBuilder fb) {
            super(fb.start, fb.size);
        }

        public Builder name(String n) {
            if (_name != null)
                throw new OleOleFlakeException("name() was already set.");
            _name = n;
            return (Builder)this;
        }

        abstract protected NamedField<byte[], V> newNameField();

        @Override
        protected void setup() {
            if (bindableFields.stream().anyMatch(f -> f.getName().equals(_name)))
                throw new OleOleFlakeException("A duplicate field name exists: " + _name);
            if (_name == null)
                _name = String.format("bindable-%d-%d", start, size);

            NamedField<byte[], V> field = newNameField();
            bindableFields.add(field);
        }
    }

    public class StringBindableFieldBuilder extends BindableFieldBuilder<StringBindableFieldBuilder, String> {

        private final StringCodec codec;

        public StringBindableFieldBuilder(FieldBuilder fb, Charset charset) {
            super(fb);
            this.codec = new StringCodec(charset);
        }

        @Override
        protected NamedField<byte[], String> newNameField() {
            return new NamedField<byte[], String>(
                    start,
                    size,
                    entireSize,
                    entireCodec,
                    codec,
                    _name,
                    invert
            );
        }
    }

    public class BytesBindableFieldBuilder extends BindableFieldBuilder<BytesBindableFieldBuilder, byte[]> {

        public BytesBindableFieldBuilder(FieldBuilder fb) {
            super(fb);
        }

        @Override
        protected NamedField<byte[], byte[]> newNameField() {
            return new NamedField<byte[], byte[]>(
                    start,
                    size,
                    entireSize,
                    entireCodec,
                    BytesCodec.singleton,
                    _name,
                    invert
            );
        }
    }

    public class LongBindableFieldBuilder extends BindableFieldBuilder<LongBindableFieldBuilder, Long> {

        public LongBindableFieldBuilder(FieldBuilder fb) {
            super(fb);
        }

        @Override
        protected NamedField<byte[], Long> newNameField() {
            return new NamedField<byte[], Long>(
                    start,
                    size,
                    entireSize,
                    entireCodec,
                    LongCodec.singleton,
                    _name,
                    invert
            );
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
            throw new OleOleFlakeException(String.format("The field size(The number of bits) is too small. Please set it to be %d bit. For unused fields, set unusedField().", entireCodec));
        if (bitPointer > entireSize)
            throw new OleOleFlakeException(String.format("The field size(The number of bits) is too large. Please set it to be %d bit. For unused fields, set unusedField().", entireCodec));

        IdBytesGen gen = new IdBytesGen(entireCodec, constantFields, bindableFields, sequence, timestamp, unusedFields, entireSize);
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
