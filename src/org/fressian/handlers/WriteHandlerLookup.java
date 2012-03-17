// Copyright (c) Metadata Partners, LLC.
// All rights reserved.

package org.fressian.handlers;

import java.util.Map;

import static org.fressian.Fns.getClassOrNull;
import static org.fressian.Fns.lookup;
import static org.fressian.Fns.soloEntry;

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
