package com.github.rerorero.oleoleflake.field;

public interface IField {
    void validate();
    int getStartBit();
    int getFieldSize();
    int getEntireSize();
}
