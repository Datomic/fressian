// Copyright (c) Metadata Partners, LLC.
// All rights reserved.

package org.fressian.impl;

import org.fressian.handlers.ILookup;

public class ChainedLookup <K,V> implements ILookup<K, V>
{
    public final ILookup<K, V> lookups [];
    public ChainedLookup(ILookup<K,V>... lookups) {
        this.lookups = lookups;
    }
    public V valAt(K key) {
        for (int i = 0; i < lookups.length; i++) {
            V val = lookups[i].valAt(key);
            if (val != null) return val;
        }
        return null;
    }
}
