//   Copyright (c) Metadata Partners, LLC. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package org.fressian.impl;

public class Ranges {

    public static final long PACKED_1_START = -1L;
    public static final long PACKED_1_END = 64;

    public static final long PACKED_2_START = 0xFFFFFFFFFFFFF000L;
    public static final long PACKED_2_END = 0x0000000000001000L;
    public static final long PACKED_3_START = 0xFFFFFFFFFFF80000L;
    public static final long PACKED_3_END = 0x0000000000080000L;
    public static final long PACKED_4_START = 0xFFFFFFFFFE000000L;
    public static final long PACKED_4_END = 0x0000000002000000L;
    public static final long PACKED_5_START = 0xFFFFFFFE00000000L;
    public static final long PACKED_5_END = 0x0000000200000000L;
    public static final long PACKED_6_START = 0xFFFFFE0000000000L;
    public static final long PACKED_6_END = 0x0000020000000000L;
    public static final long PACKED_7_START = 0xFFFE000000000000L;
    public static final long PACKED_7_END = 0x0002000000000000L;

    public static final int PRIORITY_CACHE_PACKED_END = 32;
    public static final int STRUCT_CACHE_PACKED_END = 16;
    public static final int BYTES_PACKED_LENGTH_END = 8;
    public static final int STRING_PACKED_LENGTH_END = 8;
    public static final int LIST_PACKED_LENGTH_END = 8;

    public static final int BYTE_CHUNK_SIZE = 65535;


//    public static void main(String[] args) {
//        long[] bounds = new long[]{
//                PACKED_1_START,
//                PACKED_1_END,
//                PACKED_2_START,
//                PACKED_2_END,
//                PACKED_3_START,
//                PACKED_3_END,
//                PACKED_4_START,
//                PACKED_4_END,
//                PACKED_5_START,
//                PACKED_5_END,
//                PACKED_6_START,
//                PACKED_6_END,
//                PACKED_7_START,
//                PACKED_7_END,
//        };
//        for (int n = 0; n < bounds.length; n++) {
//            for (long l = bounds[n] - 1; l < bounds[n] + 2; l++) {
//                long abs = Math.abs(l);
//                System.out.println(String.format("Number %X %d %d bits: %d switch: %d", l, l, Long.numberOfLeadingZeros(abs), bitsNeeded(l), switchOn(l)));
//            }
//        }
//    }
//    public static int bitsNeeded(long l) {
//        if (l > 0) {
//            return 65 - Long.numberOfLeadingZeros(l);
//        } else {
//            return 65 - Long.numberOfLeadingZeros(~l);
//        }
//    }
//    public static int switchOn(long l) {
//        if (l > 0) {
//            return Long.numberOfLeadingZeros(l);
//        } else {
//            return Long.numberOfLeadingZeros(~l);
//        }
//    }

}
