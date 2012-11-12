//   Copyright (c) Metadata Partners, LLC. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

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
