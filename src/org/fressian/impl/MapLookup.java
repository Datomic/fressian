// Copyright (c) Metadata Partners, LLC.
// All rights reserved.

package org.fressian.impl;

import org.fressian.handlers.ILookup;

import java.util.Map;

public class MapLookup <K,V> implements ILookup<K, V>
{
    public final Map<K, V> map;

    public MapLookup(Map<K,V> map) {
        this.map = map;
    }

    public V valAt(K key) {
        return map.get(key);
    }
}
