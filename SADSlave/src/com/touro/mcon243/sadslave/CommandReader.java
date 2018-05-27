package com.touro.mcon243.sadslave;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by amram on 5/27/2018.
 */
public class CommandReader implements ICommandReader {
    private BufferedReader reader;

    CommandReader(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public String receiveCommand() throws IOException {
        return this.reader.readLine();
    }
}
