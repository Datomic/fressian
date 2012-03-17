// Copyright (c) Metadata Partners, LLC.
// All rights reserved.

package org.fressian;

import java.util.Map;

public interface Tagged {
    public Object getTag();
    public Object getValue();
    public Map getMeta();
}
