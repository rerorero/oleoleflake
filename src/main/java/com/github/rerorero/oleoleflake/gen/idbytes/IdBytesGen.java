package com.github.rerorero.oleoleflake.gen.idbytes;

import com.github.rerorero.oleoleflake.bitset.BitSetCodec;
import com.github.rerorero.oleoleflake.field.*;
import com.github.rerorero.oleoleflake.gen.IdGen;
import com.github.rerorero.oleoleflake.gen.NextIdFactory;
import com.github.rerorero.oleoleflake.gen.SnapshotIdFactory;
import com.github.rerorero.oleoleflake.util.ByteUtil;

import java.util.List;
import java.util.Optional;

public class IdBytesGen extends IdGen<byte[], Long> {
    public IdBytesGen(
            BitSetCodec<byte[]> entireCodec,
            List<ConstantField<byte[], ?>> constantFields,
            List<NamedField<byte[], ?>> bindableFields,
            Optional<ISequentialField<byte[], Long>> sequence,
            Optional<TimestampField<byte[]>> timestamp,
            List<FieldBase> unusedFields,
            int entireBitLen
    ) {
        super(entireCodec, constantFields, bindableFields, sequence, timestamp, unusedFields, entireBitLen);
    }

    public static IdBytesGenBuilder builder(int byteLength) {
        return new IdBytesGenBuilder(byteLength);
    }

    class FixedLengthNextIdFactory extends NextIdFactory<byte[], Long> {
        public FixedLengthNextIdFactory(IdGen<byte[], Long> idGen) {
            super(idGen);
        }

        @Override
        public byte[] id() {
            return ByteUtil.paddingAhead(super.id(), entireBitLen);
        }
    }

    class FixedLengthSnapshotIdFactory extends SnapshotIdFactory<byte[], Long> {
        public FixedLengthSnapshotIdFactory(IdGen<byte[], Long> idGen) {
            super(idGen);
        }

        @Override
        public byte[] id() {
            return ByteUtil.paddingAhead(super.id(), entireBitLen);
        }
    }

    @Override
    protected NextIdFactory<byte[], Long> newNextIdFactory() {
        return new FixedLengthNextIdFactory(this);
    }

    @Override
    protected SnapshotIdFactory<byte[], Long> newSnapshotIdFactory() {
        return new FixedLengthSnapshotIdFactory(this);
    }
}
