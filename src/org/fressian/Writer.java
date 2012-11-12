//   Copyright (c) Metadata Partners, LLC. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package org.fressian;

import java.io.IOException;

public interface Writer {
    public Writer writeNull() throws IOException;
    public Writer writeBoolean(boolean b) throws IOException;
    public Writer writeBoolean(Object o) throws IOException;
    public Writer writeInt(long l) throws IOException;
    public Writer writeInt(Object o) throws IOException;
    public Writer writeDouble(double d) throws IOException;
    public Writer writeDouble(Object o) throws IOException;
    public Writer writeFloat(float d) throws IOException;
    public Writer writeFloat(Object o) throws IOException;
    public Writer writeString(Object o) throws IOException;
    public Writer writeList(Object l) throws IOException;
    public Writer writeBytes(byte[] b) throws IOException;
    public Writer writeBytes(byte[] b, int offset, int length) throws IOException;
    public Writer writeObject(Object o) throws IOException;
    public Writer writeObject(Object o, boolean cache) throws IOException;
    public Writer writeAs(String tag, Object o) throws IOException;
    public Writer writeAs(String tag, Object o, boolean cache) throws IOException;
    public Writer writeTag(Object tag, int componentCount) throws IOException;
    public Writer resetCaches() throws IOException;
    public Writer writeFooter() throws IOException;
}
