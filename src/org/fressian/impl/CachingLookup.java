// Copyright (c) Metadata Partners, LLC.
// All rights reserved.

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
