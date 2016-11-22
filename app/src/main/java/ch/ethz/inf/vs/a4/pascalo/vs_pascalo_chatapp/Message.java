package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import java.sql.Timestamp;

/**
 * Created by pascal on 22.11.16
 */

public class Message {

    private String author;

    private String msg;

    private Timestamp timestamp;

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
