// Copyright (c) Metadata Partners, LLC.
// All rights reserved.
package org.fressian.impl;

import java.io.ByteArrayOutputStream;


public class BytesOutputStream extends ByteArrayOutputStream {
    public BytesOutputStream() {
    }

    public BytesOutputStream(int i) {
        super(i);
    }

    public byte[] internalBuffer() {
        return buf;
    }

    public int length() {
        return count;
    }

}
