package com.github.rerorero.oleoleflake.field;

import com.github.rerorero.oleoleflake.OleOleFlakeException;
import com.github.rerorero.oleoleflake.bitset.BitSetCodec;

public class ConstantField<Entire, Field> extends NamedField<Entire, Field> implements Cloneable {

    private Field constantValue;

    public ConstantField(
            int start,
            int size,
            int entireSize,
            BitSetCodec<Entire> entireCodec,
            BitSetCodec<Field> fieldCodec,
            String name,
            Field value,
            boolean invert
    ) {
        super(start, size, entireSize, entireCodec, fieldCodec, name, invert);
        this.constantValue = value;
    }

    public void validate() {
        super.validate();
        if (fieldComparator().compare(constantValue, full()) > 0)
            throw new OleOleFlakeException("Constant field value ("+ name+"="+constantValue+") overflows.");
    }

    public Field getConstantValue() {
        return constantValue;
    }

    public void setConstantValue(Field value) {
        this.constantValue = value;
        validate();
    }

    public Entire putValueIntoField(Entire entire) {
        return putField(entire, getConstantValue());
    }

    @Override
    public ConstantField<Entire, Field> clone() {
        return new ConstantField<Entire, Field>(start, size, entireSize, entireCodec, fieldCodec, name, constantValue, invert);
    }
}
