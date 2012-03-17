package org.fressian.handlers;

public interface IWriteHandlerLookup {
    WriteHandler getWriteHandler(String k1, Object k2);
    WriteHandler requireWriteHandler(String tag, Object unwrapped);
}
