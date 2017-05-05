package com.github.rerorero.oleoleflake.gen;

import com.github.rerorero.oleoleflake.OleOleFlakeException;
import com.github.rerorero.oleoleflake.field.NamedField;

import java.util.BitSet;

public abstract class BindableIdFactory<Entire, Seq, F extends BindableIdFactory> {

    protected Entire entire;
    protected final IdGen<Entire, Seq> idGen;

    public BindableIdFactory(IdGen<Entire, Seq> idGen) {
        this.idGen = idGen;
        this.entire = idGen.entireCodec.toValue(new BitSet());
    }

    public <T> F bind(String name, T value) {
        NamedField<Entire, T> field = (NamedField<Entire, T>) idGen.bindableFields.get(name);
        if (field == null)
            throw new OleOleFlakeException("No such bindable field: " + name);
        entire = field.putField(entire, value);
        return (F)this;
    }

    abstract public Entire id();
}

