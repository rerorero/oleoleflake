package com.github.rerorero.oleoleflake.gen.idbytes;

import com.github.rerorero.oleoleflake.bitset.BitSetCodec;
import com.github.rerorero.oleoleflake.field.*;
import com.github.rerorero.oleoleflake.gen.IdGen;

import java.util.List;
import java.util.Optional;

public class IdBytesGen extends IdGen<byte[], Long> {
    public IdBytesGen(
            BitSetCodec<byte[]> entireCodec,
            List<ConstantField<byte[], ?>> constantFields,
            List<NamedField<byte[], ?>> bindableFields,
            Optional<ISequentialField<byte[], Long>> sequence,
            Optional<TimestampField<byte[]>> timestamp,
            List<FieldBase> unusedFields
    ) {
        super(entireCodec, constantFields, bindableFields, sequence, timestamp, unusedFields);
    }

    public static IdBytesGenBuilder builder(int idLength) {
        return new IdBytesGenBuilder(idLength);
    }
}
