package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import java.sql.Timestamp;

public class Message {

    private String author;

    private String msg;

    private Timestamp timestamp;

    public Message(String a, String m, Timestamp t) {
        author = a;
        msg = m;
        timestamp = t;
    }

    /*
    // setter functions
    public void setAuthor(String s) {
        author = s;
    }

    public void setMsg(String s) {
        msg = s;
    }

    public void setTimestamp(Timestamp t) {
        timestamp = t;
    }
    */



    // getter functions
    public String getAuthor(){
        return author;
    }

    public String getMsg() {
        return msg;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }


}
