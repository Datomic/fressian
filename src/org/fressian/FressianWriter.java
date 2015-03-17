//   Copyright (c) Metadata Partners, LLC. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package org.fressian;

import static org.fressian.impl.Fns.*;
import static org.fressian.impl.Handlers.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.*;

import org.fressian.handlers.*;
import org.fressian.impl.*;

public class FressianWriter implements StreamingWriter, Writer, Closeable {
    private OutputStream out;
    private RawOutput rawOut;
    private InterleavedIndexHopMap priorityCache;
    private InterleavedIndexHopMap structCache;
    private byte[] stringBuffer;
    IWriteHandlerLookup writeHandlerLookup;

    public FressianWriter(OutputStream out) {
        this(out, Handlers.defaultWriteHandlers());
    }

    /**
     *  Create a writer that combines userHandlers with the normal type handlers
     *  built into Fressian.
     */
    public FressianWriter(OutputStream out, ILookup<Class, Map<String, WriteHandler>> userHandlers) {
        writeHandlerLookup = new WriteHandlerLookup(userHandlers);
        clearCaches();
        this.out = out;
        this.rawOut = new RawOutput(this.out);
    }

    public Writer writeNull() throws IOException {
        writeCode(Codes.NULL);
        return this;
    }

    public Writer writeBoolean(boolean b) throws IOException {
        if (b) {
            writeCode(Codes.TRUE);
        } else {
            writeCode(Codes.FALSE);
        }
        return this;
    }

    public Writer writeBoolean(Object o) throws IOException {
        if (o == null) {
            writeNull();
            return this;
        }

        writeBoolean(((Boolean) o).booleanValue());
        return this;
    }

    public Writer writeInt(long i) throws IOException {
        internalWriteInt(i);
        return this;
    }


    public Writer writeInt(Object o) throws IOException {
        if (o == null) {
            writeNull();
            return this;
        }
        writeInt(((Number) o).longValue());
        return this;
    }

    public Writer writeDouble(double d) throws IOException {
        if (d == 0.0) {
            writeCode(Codes.DOUBLE_0);
        } else if (d == 1.0) {
            writeCode(Codes.DOUBLE_1);
        } else {
            writeCode(Codes.DOUBLE);
            rawOut.writeRawDouble(d);
        }
        return this;
    }

    public Writer writeDouble(Object o) throws IOException {
        writeDouble(((Number) o).doubleValue());
        return this;
    }

    public Writer writeFloat(float f) throws IOException {
        writeCode(Codes.FLOAT);
        rawOut.writeRawFloat(f);
        return this;
    }

    public Writer writeFloat(Object o) throws IOException {
        writeFloat(((Number) o).floatValue());
        return this;
    }

    public Writer writeString(Object o) throws IOException {
        if (o == null) {
            writeNull();
            return this;
        }
        CharSequence s = (CharSequence) o;
        int stringPos = 0;
        int bufPos = 0;
        int maxBufNeeded = Math.min(s.length() * 3, 65536);
        if ((stringBuffer == null) || (stringBuffer.length < maxBufNeeded)) {
            stringBuffer = new byte[maxBufNeeded];
        }
        do {
            int[] temp = bufferStringChunkUTF8(s, stringPos, stringBuffer);
            stringPos = temp[0];
            bufPos = temp[1];
            if (bufPos < Ranges.STRING_PACKED_LENGTH_END) {
                rawOut.writeRawByte((int) (Codes.STRING_PACKED_LENGTH_START + bufPos));
            } else if (stringPos == s.length()) {
                writeCode(Codes.STRING);
                writeCount(bufPos);
            } else {
                writeCode(Codes.STRING_CHUNK);
                writeCount(bufPos);
            }
            rawOut.writeRawBytes(stringBuffer, 0, bufPos);
        } while (stringPos < s.length());

        return this;
    }

    private void writeIterator(int length, Iterator it) throws IOException {
        if (length < Ranges.LIST_PACKED_LENGTH_END) {
            rawOut.writeRawByte((int) (Codes.LIST_PACKED_LENGTH_START + length));
        } else {
            writeCode(Codes.LIST);
            writeCount(length);
        }
        while (it.hasNext()) {
            writeObject(it.next());
        }
    }

    public Writer writeList(Object o) throws IOException {
        if (o == null) {
            writeNull();
            return this;
        }
        if (o.getClass().isArray()) {
            return writeList(Arrays.asList(o));
        }
        Collection c = (Collection) o;
        writeIterator(c.size(), c.iterator());
        return this;
    }

    public Writer writeBytes(byte[] b) throws IOException {
        if (b == null) {
            writeNull();
            return this;
        }

        return writeBytes(b, 0, b.length);
    }

    public Writer writeBytes(byte[] b, int offset, int length) throws IOException {
        if (length < Ranges.BYTES_PACKED_LENGTH_END) {
            rawOut.writeRawByte((int) (Codes.BYTES_PACKED_LENGTH_START + length));
            rawOut.writeRawBytes(b, offset, length);
        } else {
            while (length > Ranges.BYTE_CHUNK_SIZE) {
                writeCode(Codes.BYTES_CHUNK);
                writeCount(Ranges.BYTE_CHUNK_SIZE);
                rawOut.writeRawBytes(b, offset, Ranges.BYTE_CHUNK_SIZE);
                offset += Ranges.BYTE_CHUNK_SIZE;
                length -= Ranges.BYTE_CHUNK_SIZE;
            }
            writeCode(Codes.BYTES);
            writeCount(length);
            rawOut.writeRawBytes(b, offset, length);
        }
        return this;
    }

    public void writeFooterFor(ByteBuffer bb) throws IOException {
        if (rawOut.getBytesWritten() != 0)
            throw new IllegalStateException("writeFooterFor can only be called at a footer boundary.");
        ByteBuffer source = bb.duplicate();
        byte[] bytes;
        if (source.hasArray()) {
            bytes = source.array();
        } else {
            bytes = new byte[source.remaining()];
            source.get(bytes);
        }
        rawOut.getChecksum().update(bytes, 0, source.remaining());
        internalWriteFooter(source.remaining());
    }

    public Writer writeFooter() throws IOException {
        internalWriteFooter(rawOut.getBytesWritten());
        clearCaches();
        return this;
    }

    private void internalWriteFooter(int length) throws IOException {
        rawOut.writeRawInt32(Codes.FOOTER_MAGIC);
        rawOut.writeRawInt32(length);
        rawOut.writeRawInt32((int) rawOut.getChecksum().getValue());
        rawOut.reset();
    }

    private void clearCaches() {
        if ((priorityCache != null) && !priorityCache.isEmpty())
            priorityCache.clear();
        if ((structCache != null) && !structCache.isEmpty())
            structCache.clear();
    }

    public Writer resetCaches() throws IOException {
        writeCode(Codes.RESET_CACHES);
        clearCaches();
        return this;
    }

    public InterleavedIndexHopMap getPriorityCache() {
        if (priorityCache == null) {
            priorityCache = new InterleavedIndexHopMap(16);
        }
        return priorityCache;
    }

    public InterleavedIndexHopMap getStructCache() {
        if (structCache == null) {
            structCache = new InterleavedIndexHopMap(16);
        }
        return structCache;
    }

    public Writer writeTag(Object tag, int componentCount) throws IOException {
        Integer shortcutCode = tagToCode.get(tag);
        if (shortcutCode != null) {
            writeCode(shortcutCode);
        } else {
            int index = getStructCache().oldIndex(tag);
            if (index == -1) {
                writeCode(Codes.STRUCTTYPE);
                writeObject(tag);
                writeInt(componentCount);
            } else if (index < Ranges.STRUCT_CACHE_PACKED_END) {
                writeCode((int) Codes.STRUCT_CACHE_PACKED_START + index);
            } else {
                writeCode(Codes.STRUCT);
                writeInt(index);
            }
        }
        return this;
    }

    public Writer writeExt(Object tag, Object... fields) throws IOException {
        writeTag(tag, fields.length);
        for (int n = 0; n < fields.length; n++) {
            writeObject(fields[n]);
        }
        return this;
    }

    public void writeCount(int count) throws IOException {
        writeInt(count);
    }

    // returns (bits not needed to represent this number) + 1
    private int bitSwitch(long l) {
        if (l < 0) l = ~l;
        return Long.numberOfLeadingZeros(l);
    }

    private void internalWriteInt(long i) throws IOException {

        switch (bitSwitch(i)) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
                writeCode(Codes.INT);
                rawOut.writeRawInt64(i);
                break;

            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
                rawOut.writeRawByte((int) (Codes.INT_PACKED_7_ZERO + (i >> 48)));
                rawOut.writeRawInt48(i);
                break;

            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
                rawOut.writeRawByte((int) (Codes.INT_PACKED_6_ZERO + (i >> 40)));
                rawOut.writeRawInt40(i);
                break;

            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
                rawOut.writeRawByte((int) (Codes.INT_PACKED_5_ZERO + (i >> 32)));
                rawOut.writeRawInt32((int) i);
                break;

            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
                rawOut.writeRawByte((int) (Codes.INT_PACKED_4_ZERO + (i >> 24)));
                rawOut.writeRawInt24((int) i);
                break;

            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
            case 50:
            case 51:
                rawOut.writeRawByte((int) (Codes.INT_PACKED_3_ZERO + (i >> 16)));
                rawOut.writeRawInt16((int) i);
                break;

            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
                rawOut.writeRawByte((int) (Codes.INT_PACKED_2_ZERO + (i >> 8)));
                rawOut.writeRawByte((int) i);
                break;

            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
                if (i < -1) {
                    rawOut.writeRawByte((int) (Codes.INT_PACKED_2_ZERO + (i >> 8)));
                }
                rawOut.writeRawByte((int) i);
                break;

            default:
                throw new Error("more than 64 bits in a long!");
        }
    }

    private boolean shouldSkipCache(Object o) {
        if ((o == null) || (o instanceof Boolean))
            return true;
        else if ((o instanceof Integer) || (o instanceof Short) || (o instanceof Long)) {
            switch (bitSwitch(((Number) o).longValue())) {
                // current: 1 or 2 byte representations skip cache
                // consider: cache two byte reps after checking priority cache
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                    return true;
                default:
                    return false;

            }
        } else if (o instanceof String)
            return ((String) o).length() == 0;
        else if (o instanceof Double) {
            double d = ((Double) o).doubleValue();
            return (d == 0.0) || (d == 1.0);
        }
        return false;
    }

    private void doWrite(String tag, Object o, WriteHandler w, boolean cache) throws IOException {
        if (cache) {
            if (shouldSkipCache(o))
                doWrite(tag, o, w, false);
            else {
                int index = getPriorityCache().oldIndex(o);
                if (index == -1) {
                    writeCode(Codes.PUT_PRIORITY_CACHE);
                    doWrite(tag, o, w, false);
                } else {
                    if (index < Ranges.PRIORITY_CACHE_PACKED_END) {
                        writeCode((int) Codes.PRIORITY_CACHE_PACKED_START + index);
                    } else {
                        writeCode(Codes.GET_PRIORITY_CACHE);
                        writeInt(index);
                    }
                }
            }
        } else {
            w.write(this, o);
        }
    }

    public Writer writeAs(String tag, Object o, boolean cache) throws IOException {
        if (o instanceof CachedObject) {
            o = CachedObject.unwrap(o);
            cache = true;
        }
        WriteHandler w = writeHandlerLookup.requireWriteHandler(tag, o);
        doWrite(tag, o, w, cache);
        return this;
    }

    public Writer writeAs(String tag, Object o) throws IOException {
        return writeAs(tag, o, false);
    }

    public Writer writeObject(Object o, boolean cache) throws IOException {
        return writeAs(null, o, cache);
    }

    public Writer writeObject(Object o) throws IOException {
        return writeAs(null, o);
    }

    public void writeCode(int code) throws IOException {
        rawOut.writeRawByte(code);
    }

    public void close() throws IOException {
        rawOut.close();
    }

    public Writer beginClosedList() throws IOException {
        writeCode(Codes.BEGIN_CLOSED_LIST);
        return this;
    }

    public Writer endList() throws IOException {
        writeCode(Codes.END_COLLECTION);
        return this;
    }

    public Writer beginOpenList() throws IOException {
        if (0 != rawOut.getBytesWritten())
            throw new IllegalStateException("openList must be called from the top level, outside any footer context.");
        writeCode(Codes.BEGIN_OPEN_LIST);
        return this;
    }
}
