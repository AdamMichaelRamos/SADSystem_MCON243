package com.touro.mcon243.sadslave;

import java.io.IOException;

/**
 * Created by amram on 5/27/2018.
 */
public interface ICommandReader {
    String receiveCommand() throws IOException;
}
