//   Copyright (c) Metadata Partners, LLC. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

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
