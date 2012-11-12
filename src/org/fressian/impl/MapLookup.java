//   Copyright (c) Metadata Partners, LLC. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

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
