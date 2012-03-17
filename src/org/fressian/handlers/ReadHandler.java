// Copyright (c) Metadata Partners, LLC.
// All rights reserved.

package org.fressian.handlers;

import org.fressian.Reader;

import java.io.IOException;

public interface ReadHandler {
    public Object read(Reader r, Object tag, int componentCount) throws IOException;
}
