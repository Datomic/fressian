//   Copyright (c) Metadata Partners, LLC. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

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
