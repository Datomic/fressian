//   Copyright (c) Metadata Partners, LLC. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package org.fressian.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * <code>InputStream</code> over a <code>ByteBuffer</code>. Duplicates the
 * buffer on construction in order to maintain its own cursor.
 *
 * @see     java.io.InputStream
 * @see     java.nio.ByteBuffer
 */
public class ByteBufferInputStream extends InputStream {
    private final ByteBuffer buf;

    public ByteBufferInputStream(ByteBuffer buf) {
        this.buf = buf.duplicate();
    }

    @Override
    public synchronized void reset() throws IOException {
        buf.reset();
    }

    @Override
    public synchronized void mark(int readlimit) {
        buf.mark();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read() throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }
        int result = buf.get();
        return result & 0xff;
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        if (len == 0) return 0;
        int bytesRead = Math.min((int) len, buf.remaining());
        if (bytesRead <= 0) {
            return -1;
        }
        buf.get(bytes, off, bytesRead);
        return bytesRead;
    }

    @Override
    public long skip(long l) throws IOException {
        // note: buf.remaining() can be negative
        int skipped = Math.min((int) l, buf.remaining());
        if (skipped <= 0) {
            return 0;
        }
        buf.position(buf.position() + skipped);
        return skipped;
    }

    @Override
    public int available() throws IOException {
        return buf.remaining();
    }
}
