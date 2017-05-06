package com.github.rerorero.oleoleflake.gen.id64;

import com.github.rerorero.oleoleflake.bitset.BitSetCodec;
import com.github.rerorero.oleoleflake.field.*;
import com.github.rerorero.oleoleflake.gen.IdGen;

import java.util.List;
import java.util.Optional;

public class Id64Gen extends IdGen<Long, Long> {
    public Id64Gen(
            BitSetCodec<Long> entireCodec,
            List<ConstantField<Long, ?>> constantFields,
            List<NamedField<Long, ?>> bindableFields,
            Optional<ISequentialField<Long, Long>> sequence,
            Optional<TimestampField<Long>> timestamp,
            List<FieldBase> unusedFields
    ) {
        super(entireCodec, constantFields, bindableFields, sequence, timestamp, unusedFields);
    }

    public static Id64GenBuilder builder() {
        return new Id64GenBuilder();
    }
}
