// Copyright (c) Metadata Partners, LLC.
// All rights reserved.

package org.fressian.handlers;

import org.fressian.Writer;

import java.io.IOException;

public interface WriteHandler {
    public void write(Writer w, Object instance) throws IOException;
}
