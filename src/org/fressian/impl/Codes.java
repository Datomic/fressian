//   Copyright (c) Metadata Partners, LLC. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package org.fressian.impl;

public class Codes {
    public static final int PRIORITY_CACHE_PACKED_START = 0x80;
    public static final int PRIORITY_CACHE_PACKED_END = 0xA0;
    public static final int STRUCT_CACHE_PACKED_START = 0xA0;
    public static final int STRUCT_CACHE_PACKED_END = 0xB0;
    public static final int LONG_ARRAY = 0xB0;
    public static final int DOUBLE_ARRAY = 0xB1;
    public static final int BOOLEAN_ARRAY = 0xB2;
    public static final int INT_ARRAY = 0xB3;
    public static final int FLOAT_ARRAY = 0xB4;
    public static final int OBJECT_ARRAY = 0xB5;
    public static final int MAP = 0xC0;
    public static final int SET = 0xC1;
    public static final int UUID = 0xC3;
    public static final int REGEX = 0xC4;
    public static final int URI = 0xC5;
    public static final int BIGINT = 0xC6;
    public static final int BIGDEC = 0xC7;
    public static final int INST = 0xC8;
    public static final int SYM = 0xC9;
    public static final int KEY = 0xCA;
    public static final int GET_PRIORITY_CACHE = 0xCC;
    public static final int PUT_PRIORITY_CACHE = 0xCD;
    public static final int PRECACHE = 0xCE;
    public static final int FOOTER = 0xCF;
    public static final int FOOTER_MAGIC = 0xCFCFCFCF;
    public static final int BYTES_PACKED_LENGTH_START = 0xD0;
    public static final int BYTES_PACKED_LENGTH_END = 0xD8;
    public static final int BYTES_CHUNK = 0xD8;
    public static final int BYTES = 0xD9;
    public static final int STRING_PACKED_LENGTH_START = 0xDA;
    public static final int STRING_PACKED_LENGTH_END = 0xE2;
    public static final int STRING_CHUNK = 0xE2;
    public static final int STRING = 0xE3;
    public static final int LIST_PACKED_LENGTH_START = 0xE4;
    public static final int LIST_PACKED_LENGTH_END = 0xEC;
    public static final int LIST = 0xEC;
    public static final int BEGIN_CLOSED_LIST = 0xED;
    public static final int BEGIN_OPEN_LIST = 0xEE;
    public static final int STRUCTTYPE = 0xEF;
    public static final int STRUCT = 0xF0;
    public static final int META = 0xF1;
    public static final int ANY = 0xF4;
    public static final int TRUE = 0xF5;
    public static final int FALSE = 0xF6;
    public static final int NULL = 0xF7;
    public static final int INT = 0xF8;
    public static final int FLOAT = 0xF9;
    public static final int DOUBLE = 0xFA;
    public static final int DOUBLE_0 = 0xFB;
    public static final int DOUBLE_1 = 0xFC;
    public static final int END_COLLECTION = 0xFD;
    public static final int RESET_CACHES = 0xFE;
    public static final int INT_PACKED_1_START = 0xFF;
    public static final int INT_PACKED_1_END = 0x40;
    public static final int INT_PACKED_2_START = 0x40;
    public static final int INT_PACKED_2_ZERO = 0x50;
    public static final int INT_PACKED_2_END = 0x60;
    public static final int INT_PACKED_3_START = 0x60;
    public static final int INT_PACKED_3_ZERO = 0x68;
    public static final int INT_PACKED_3_END = 0x70;
    public static final int INT_PACKED_4_START = 0x70;
    public static final int INT_PACKED_4_ZERO = 0x72;
    public static final int INT_PACKED_4_END = 0x74;
    public static final int INT_PACKED_5_START = 0x74;
    public static final int INT_PACKED_5_ZERO = 0x76;
    public static final int INT_PACKED_5_END = 0x78;
    public static final int INT_PACKED_6_START = 0x78;
    public static final int INT_PACKED_6_ZERO = 0x7A;
    public static final int INT_PACKED_6_END = 0x7C;
    public static final int INT_PACKED_7_START = 0x7C;
    public static final int INT_PACKED_7_ZERO = 0x7E;
    public static final int INT_PACKED_7_END = 0x80;
}
