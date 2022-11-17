package org.fressian.impl;

import java.io.EOFException;
import java.io.IOException;

public interface InputSpi {
    public int readRawByte() throws IOException;
    public long readRawInt32() throws IOException;
    public long readRawInt64() throws IOException;
    public float readRawFloat() throws IOException;
    public double readRawDouble() throws IOException;
    public void readFully(byte [] bytes, int offset, int length) throws IOException;

    default public long readRawInt8() throws IOException {
        return readRawByte();
    }
    default public long readRawInt16() throws IOException {
        return (readRawByte() << 8) + readRawByte();
    }
    default public long readRawInt24() throws IOException {
        return (readRawByte() << 16) + (readRawByte() << 8) + readRawByte();
    }

    default public long readRawInt40() throws IOException {
        return (readRawInt8() << 32) | readRawInt32();
    }

    default public long readRawInt48() throws IOException {
        return (readRawInt16() << 32) | readRawInt32();
    }
}
