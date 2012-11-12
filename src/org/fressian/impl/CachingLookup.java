//   Copyright (c) Metadata Partners, LLC. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package org.fressian.impl;

import org.fressian.handlers.ILookup;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

// could refine to keep track of lookup misses...
public class CachingLookup <K,V> implements ILookup<K, V>
{
    public final ILookup<K, V> lookup;
    public final ConcurrentHashMap<K, V> map = new ConcurrentHashMap();
    public final AtomicReference<V> nullKeyValue = new AtomicReference(null);

    public CachingLookup(ILookup<K,V> lookup) {
        this.lookup = lookup;
    }

    private V getNullVal() {
        V val = nullKeyValue.get();
        if (val != null) return val;
        val = lookup.valAt(null);
        if (val != null) nullKeyValue.getAndSet(val);
        return val;
    }

    public V valAt(K key) {
        if (key == null) return getNullVal();
        V val = map.get(key);
        if (val != null) return val;
        val = lookup.valAt(key);
        if (val != null) map.putIfAbsent(key, val);
        return val;
    }
}
