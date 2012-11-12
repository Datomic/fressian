//   Copyright (c) Metadata Partners, LLC. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package org.fressian.impl;

import org.fressian.handlers.ILookup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Fns {
    public static IllegalArgumentException expected(Object expected, int ch) {
        return new IllegalArgumentException(String.format("expected %s at %X", expected, ch));
    }

    public static IllegalArgumentException expected(Object expected, int ch, Object got) {
        return new IllegalArgumentException(String.format("expected %s at %X, got %s", expected, ch, got));
    }

    public static <K,V> Map.Entry<K, V> soloEntry(Map<K, V> m) {
        if ((m != null) && m.size() == 1) {
            return m.entrySet().iterator().next();
        }
        throw new IllegalArgumentException(String.format("expected a map of one entry, got %s", m));
    }

    public static byte[] UUIDtoByteArray(UUID uuid) {
        byte[] bytes = new byte[16];
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bytes;
    }

    public static UUID byteArrayToUUID(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        return new UUID(bb.getLong(), bb.getLong());
    }

    public static <K,V> K soloKey(Map <K, V> m) {
        return soloEntry(m).getKey();
    }

    public static <K,V> V soloVal(Map <K, V> m) {
        return soloEntry(m).getValue();
    }

    public static <K, V> Map<K,V> soloMap(K k, V v) {
        Map<K,V> m = new HashMap<K, V>();
        m.put(k, v);
        return m;
    }

    public static <K, V> V lookup(ILookup<K, V> theLookup, K k) {
        if (theLookup == null) return null;
        return theLookup.valAt(k);
    }

    public static Class getClassOrNull(Object o) {
        if (o == null) {
            return null;
        } else {
            return o.getClass();
        }
    }
    public static int intCast(long x){
        int i = (int) x;
        if(i != x)
            throw new IllegalArgumentException("Value out of range for int: " + x);
        return i;
    }

    public static void readUTF8Chars(StringBuffer dest, byte[] source, int offset, int length) {
        for (int pos = offset; pos < length;) {
            int ch = (int)source[pos++] & 0xff;
            switch (ch >> 4)
                {                       
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    dest.append((char)ch);
                    break;
                case 12:
                case 13:
                    {
                    int ch1 = source[pos++];
                    dest.append( (char) ((ch & 0x1f) << 6 | ch1 & 0x3f) );
                    }
                    break;
                case 14:
                    {
                    int ch1 = source[pos++];
                    int ch2 = source[pos++];
                    dest.append( (char) ((ch & 0x0f) << 12 | (ch1 & 0x3f) << 6 | ch2 & 0x3f) );
                    }
                    break;
                default:
                    throw new RuntimeException(String.format("Invalid UTF-8: %X", ch));
                }
            }
        }

    public static int utf8EncodingSize(int ch) {
        if (ch <= 0x007f)
            return 1;
        else if (ch > 0x07ff)
            return 3;
        return 2;
    }

    // starting with position start in s, write as much of s as possible into byteBuffer
    // using UTF-8. 
    // returns {stringpos, bufpos}
    public static int[] bufferStringChunkUTF8(CharSequence s, int start, byte[] byteBuffer) throws IOException {
        int bufferPos = 0;
        int stringPos = start;
        while (stringPos < s.length()) {
            char ch = s.charAt(stringPos);
            int encodingSize = utf8EncodingSize(ch);
            if ((bufferPos + encodingSize) > byteBuffer.length) {
                break;
            }

            switch (encodingSize) {
                case 1:
                    byteBuffer[bufferPos++] = (byte) ch;
                    break;
                case 2:
                    byteBuffer[bufferPos++] = (byte) (0xc0 | ch >> 6 & 0x1f);
                    byteBuffer[bufferPos++] = (byte) (0x80 | ch >> 0 & 0x3f);
                    break;
                case 3:
                    byteBuffer[bufferPos++] = (byte) (0xe0 | ch >> 12 & 0x0f);
                    byteBuffer[bufferPos++] = (byte) (0x80 | ch >> 6 & 0x3f);
                    byteBuffer[bufferPos++] = (byte) (0x80 | ch >> 0 & 0x3f);
                    break;
            }
            stringPos++;
        }
        return new int[] {stringPos, bufferPos};
    }


}
