package com.touro.mcon243.sadslave.tests;

import com.touro.mcon243.sadslave.SADSlave;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by amram on 5/27/2018.
 */
public class SlaveTests {
    @Test
    public void test() throws IOException, InterruptedException {
        SADSlave.receiveCommandAndPerformJob(
                "TestSlave",
                () -> "mock command",
                () -> System.out.println("mockWorkGenerator| perform work"));
    }
}
