//   Copyright (c) Metadata Partners, LLC. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package org.fressian.handlers;

import org.fressian.impl.CachingLookup;
import org.fressian.impl.ChainedLookup;
import org.fressian.impl.Handlers;

import java.util.Map;

import static org.fressian.impl.Fns.getClassOrNull;
import static org.fressian.impl.Fns.lookup;
import static org.fressian.impl.Fns.soloEntry;

public class WriteHandlerLookup implements IWriteHandlerLookup {
    public static ILookup<Class, Map<String, WriteHandler>>
    createLookupChain(ILookup<Class, Map<String, WriteHandler>> userHandlers)  {
        if (userHandlers != null) {
            return new CachingLookup(new ChainedLookup(Handlers.coreWriteHandlers, userHandlers, Handlers.extendedWriteHandlers));
        } else {
            return Handlers.defaultWriteHandlers();
        }
    }

    // throws an exception if writer does not exist or does not match tag.
    //  Pass null tag to skip tag check.
    public WriteHandler getWriteHandler(String tag, Object o) {
        Map<String, WriteHandler> h = lookup(chainedLookup, getClassOrNull(o));
        if (h == null)
            return null;
        Map.Entry<String, WriteHandler> taggedWriter = soloEntry(h);
        if (tag != null && !tag.equals(taggedWriter.getKey()) && !taggedWriter.getKey().equals("any")) {
            return null;
        } else {
            return taggedWriter.getValue();
        }
    }

    public WriteHandler requireWriteHandler(String tag, Object o) {
        WriteHandler handler = getWriteHandler(tag, o);
        if (handler == null)
            throw new IllegalArgumentException("Cannot write " + o + " as tag " + tag);
        return handler;
    }

    private final ILookup<Class, Map<String, WriteHandler>> chainedLookup;

    public WriteHandlerLookup(ILookup<Class, Map<String, WriteHandler>> userHandlers) {
        this.chainedLookup = WriteHandlerLookup.createLookupChain(userHandlers);
    }
}
