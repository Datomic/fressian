// Copyright (c) Metadata Partners, LLC.
// All rights reserved.

package org.fressian;

import java.io.IOException;

public interface Reader {
    public boolean readBoolean() throws IOException;
    public long readInt() throws IOException;
    public double readDouble() throws IOException;
    public float readFloat() throws IOException;
    public Object readObject() throws IOException;
    public void validateFooter() throws IOException;
}
