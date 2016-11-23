package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import java.sql.Timestamp;

public class Message {

    private boolean writtenByMe;

    private String msg;

    private Timestamp timestamp;

    private boolean acked = false;

    public Message(boolean w, String m, Timestamp t) {
        writtenByMe = w;
        msg = m;
        timestamp = t;
    }

    public void setAcked() {
        acked = true;
    }


    // getter functions
    public boolean getWrittenByus() {
        return writtenByMe;
    }

    public String getMsg() {
        return msg;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }


    public boolean isAcked() {
        return acked;
    }
}
