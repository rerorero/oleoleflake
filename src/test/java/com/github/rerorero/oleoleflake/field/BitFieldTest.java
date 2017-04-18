package com.github.rerorero.oleoleflake.field;

import org.junit.Test;
import static org.junit.Assert.*;

public class BitFieldTest {

    @Test
    public void getFromTest() throws Exception {
        BitField sut1 = new BitField(1,4);    // _xxxx___ ________ ________ ________ ________ ________ ________ ________
        BitField sut2 = new BitField(8,30);   // ________ xxxxxxxx xxxxxxxx xxxxxxxx xxxxxx__ ________ ________ ________
        BitField sut3 = new BitField(60,4);   // ________ ________ ________ ________ ________ ________ ________ ____xxxx

        assertEquals(0b0011100000000000000000000000000000000000000000000000000000000000L, sut1.setTo(0, 7));
    }
}
