//   Copyright (c) Metadata Partners, LLC. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package org.fressian.impl;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;

public class RawOutput implements Closeable {
    private final CheckedOutputStream os;
    private int bytesWritten;

    public RawOutput(OutputStream os) {
        this.os = new CheckedOutputStream(os, new Adler32());
    }

    public void writeRawByte(int b) throws IOException {
        os.write(b);
        notifyBytesWritten(1);
    }

    public void writeRawInt16(int s) throws IOException {
        os.write((s >>> 8) & 0xFF);
        os.write(s & 0xFF);
        notifyBytesWritten(2);
    }

    public void writeRawInt24(int i) throws IOException {
        os.write((i >>> 16) & 0xFF);
        os.write((i >>> 8) & 0xFF);
        os.write(i & 0xFF);
        notifyBytesWritten(3);
    }

    public void writeRawInt32(int i) throws IOException {
        os.write((i >>> 24) & 0xFF);
        os.write((i >>> 16) & 0xFF);
        os.write((i >>> 8) & 0xFF);
        os.write(i & 0xFF);
        notifyBytesWritten(4);
    }

    public void writeRawInt40(long i) throws IOException {
        os.write((int) (i >>> 32) & 0xFF);
        os.write((int) (i >>> 24) & 0xFF);
        os.write((int) (i >>> 16) & 0xFF);
        os.write((int) (i >>> 8) & 0xFF);
        os.write((int) i & 0xFF);
        notifyBytesWritten(5);
    }

    public void writeRawInt48(long i) throws IOException {
        os.write((int) (i >>> 40) & 0xFF);
        os.write((int) (i >>> 32) & 0xFF);
        os.write((int) (i >>> 24) & 0xFF);
        os.write((int) (i >>> 16) & 0xFF);
        os.write((int) (i >>> 8) & 0xFF);
        os.write((int) i & 0xFF);
        notifyBytesWritten(6);
    }

    byte[] buffer = new byte[8];
    public void writeRawInt64(long l) throws IOException {
        buffer[0] = (byte)(l >>> 56);
        buffer[1] = (byte)(l >>> 48);
        buffer[2] = (byte)(l >>> 40);
        buffer[3] = (byte)(l >>> 32);
        buffer[4] = (byte)(l >>> 24);
        buffer[5] = (byte)(l >>> 16);
        buffer[6] = (byte)(l >>>  8);
        buffer[7] = (byte)(l >>>  0);
        os.write(buffer, 0, 8);
        notifyBytesWritten(8);
    }

    public void writeRawDouble(double d) throws IOException {
        writeRawInt64(Double.doubleToLongBits(d));
    }

    public void writeRawFloat(float f) throws IOException {
        writeRawInt32(Float.floatToIntBits(f));
    }

    public void writeRawBytes(byte[] bytes, int off, int len) throws IOException {
        os.write(bytes, off, len);
        notifyBytesWritten(len);
    }

    public Checksum getChecksum(){
        return os.getChecksum();
    }

    public int getBytesWritten() {
        return bytesWritten;    
    }

    public void reset() {
        bytesWritten = 0;
        getChecksum().reset();
    }

    private void notifyBytesWritten(int count) {
        bytesWritten = bytesWritten + count;
    }

    public void close() throws IOException {
        os.close();
    }
}
