// Copyright (c) Metadata Partners, LLC.
// All rights reserved.

package org.fressian;

import org.fressian.Tagged;

import java.util.Map;

public class TaggedObject implements Tagged {
    public final Object tag;
    public final Object[] value;
    public final Map meta;

    public TaggedObject(Object tag, Object[] value) {
        this(tag, value, null);
    }

    public TaggedObject(Object tag, Object[] value, Map meta) {
        this.meta = meta;
        this.value = value;
        this.tag = tag;
    }

    public Object getTag() {
        return tag;
    }

    public Object[] getValue() {
        return value;
    }

    public Map getMeta() {
        return meta;
    }
}
