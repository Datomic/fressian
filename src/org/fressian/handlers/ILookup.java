// Copyright (c) Metadata Partners, LLC.
// All rights reserved.

package org.fressian.handlers;

public interface ILookup<K, V> {
    V valAt(K key);
}
