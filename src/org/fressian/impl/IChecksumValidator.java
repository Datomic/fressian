package org.fressian.impl;

import java.io.IOException;

public interface IChecksumValidator {
    int getBytesRead();

    void reset();

    void validateChecksum() throws IOException;
}
