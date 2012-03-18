// Copyright (c) Metadata Partners, LLC.
// All rights reserved.

package org.fressian.impl;

public class StructType {
    public final Object tag;
    public final int fields;

    public StructType(Object tag, int fields) {
        this.tag = tag;
        this.fields = fields;
    }
}
