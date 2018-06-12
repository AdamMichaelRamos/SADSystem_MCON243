package com.touro.mcon243.sadmaster.slave;

import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 * Created by amram on 6/12/2018.
 */
public class SlaveFrame {
    private static final String WORKING = "working", IDLE = "idle", DEAD = "dead";

    public final String[] status;
    public final String name;
    public final BufferedReader reader;
    public final BufferedWriter writer;

    public SlaveFrame(String name, BufferedReader reader, BufferedWriter writer) {
        this.status = new String[]{ SlaveFrame.IDLE };
        this.name = name;
        this.reader = reader;
        this.writer = writer;
    }

    public boolean isIdle() {
        return this.status[0].equalsIgnoreCase(SlaveFrame.IDLE);
    }

    public boolean isAlive() {
        return !this.status[0].equalsIgnoreCase(SlaveFrame.DEAD);
    }

    public void setAsIdle() {
        this.status[0] = SlaveFrame.IDLE;
    }

    public void setAsWorking() {
        this.status[0] = SlaveFrame.WORKING;
    }

    public void setDead() {
        this.status[0] = SlaveFrame.DEAD;
    }
}
