package org.fressian.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ByteBufferInput implements InputSpi {
    private final ByteBuffer bb;

    public ByteBufferInput(ByteBuffer source) {
        bb = source.duplicate();
    }
    @Override
    public int readRawByte() throws IOException {
        return bb.get() & 0xff;
    }

    @Override
    public long readRawInt32() throws IOException {
        return bb.getInt() & 0xffff;
    }

    @Override
    public long readRawInt64() throws IOException {
        return bb.getLong();
    }

    @Override
    public float readRawFloat() throws IOException {
        return bb.getFloat();
    }

    @Override
    public double readRawDouble() throws IOException {
        return bb.getDouble();
    }

    @Override
    public void readFully(byte[] bytes, int offset, int length) throws IOException {
        bb.get(bytes, offset, length);
    }
}
