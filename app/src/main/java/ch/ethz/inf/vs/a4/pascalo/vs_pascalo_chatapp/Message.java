package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import java.sql.Timestamp;

public class Message {

    private boolean writtenByMe;

    private String msg;

    private Timestamp timestamp;

    public Message(boolean w, String m, Timestamp t) {
        writtenByMe = w;
        msg = m;
        timestamp = t;
    }


    // getter functions
    public boolean getWrittenByus(){
        return writtenByMe;
    }

    public String getMsg() {
        return msg;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }


}
