// Copyright (c) Metadata Partners, LLC.
// All rights reserved.

package org.fressian;

public class CachedObject implements Cached {
    public final Object objectToCache;

    public CachedObject(Object objectToCache) {
        this.objectToCache = objectToCache;
    }

    public Object getObjectToCache() {
        return objectToCache;
    }

    public static Object unwrap(Object o) {
        if (o instanceof Cached) {
            return ((Cached)o).getObjectToCache();
        }
        return o;
    }
}
