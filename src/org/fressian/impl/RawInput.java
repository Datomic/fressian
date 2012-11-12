//   Copyright (c) Metadata Partners, LLC. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package org.fressian.impl;

import java.io.*;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

public class RawInput implements Closeable {
    private final InputStream is;
    private final CheckedInputStream cis;
    private final DataInputStream dis;
    private int bytesRead;

    public RawInput(InputStream is) {
        this(is, true);
    }

    public RawInput(InputStream is, boolean validateAdler) {
        if (validateAdler) {
            this.cis = new CheckedInputStream(is, new Adler32());
            this.is = cis;
        } else {
            this.is = is;
            this.cis = null;
        }
        this.dis = new DataInputStream(this.is);
    }

    public int readRawByte() throws IOException {
        int result = is.read();
        if (result < 0) {
            throw new EOFException();
        }
        bytesRead++;
        return result;
    }

    public long readRawInt8() throws IOException {
        return readRawByte();
    }

    public long readRawInt16() throws IOException {
        return (readRawByte() << 8) + readRawByte();
    }

    public long readRawInt24() throws IOException {
        return (readRawByte() << 16) + (readRawByte() << 8) + readRawByte();
    }

    public long readRawInt32() throws IOException {
        bytesRead = bytesRead + 4;
        return (long) dis.readInt() & 0xffffffffL;
    }

    public long readRawInt40() throws IOException {
        return (readRawInt8() << 32) | readRawInt32();
    }

    public long readRawInt48() throws IOException {
        return (readRawInt16() << 32) | readRawInt32();
    }

    public long readRawInt64() throws IOException {
        bytesRead = bytesRead + 8;
        return dis.readLong();
    }

    public float readRawFloat() throws IOException {
        bytesRead = bytesRead + 4;
        return dis.readFloat();
    }

    public double readRawDouble() throws IOException {
        bytesRead = bytesRead + 8;
        return dis.readDouble();
    }

    public void readFully(byte [] bytes, int offset, int length) throws IOException {
        dis.readFully(bytes, offset, length);
        bytesRead += length;
    }

    public int getBytesRead() {
        return bytesRead;
    }

    public void reset() {
        bytesRead = 0;
        if (cis != null) cis.getChecksum().reset();
    }

    public void close() throws IOException {
        is.close();
    }

    public void validateChecksum() throws IOException {
        if (cis == null) {
            readRawInt32();
        } else {
            int calculatedChecksum = (int) cis.getChecksum().getValue();
            int checksumFromStream = (int) readRawInt32();
            if (calculatedChecksum != checksumFromStream) {
                throw new RuntimeException(String.format("Invalid footer checksum, expected %X got %X", calculatedChecksum, checksumFromStream));
            }
        }
    }
}
