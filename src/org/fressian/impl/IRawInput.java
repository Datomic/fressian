package org.fressian.impl;

import java.io.Closeable;
import java.io.IOException;

public interface IRawInput extends Closeable {
    int readRawByte() throws IOException;

    long readRawInt8() throws IOException;

    long readRawInt16() throws IOException;

    long readRawInt24() throws IOException;

    long readRawInt32() throws IOException;

    long readRawInt40() throws IOException;

    long readRawInt48() throws IOException;

    long readRawInt64() throws IOException;

    float readRawFloat() throws IOException;

    double readRawDouble() throws IOException;

    void readFully(byte[] bytes, int offset, int length) throws IOException;

    int getBytesRead();

    void reset();

    void validateChecksum() throws IOException;
}
