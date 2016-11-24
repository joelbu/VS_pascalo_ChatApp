package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

public class Message {

    private boolean writtenByMe;
    private boolean acked = false;

    // For user display
    private Calendar timeWritten;

    // For ordering and acking
    private int myVectorClock;
    private int theirVectorClock;

    // Actual text written by user
    private String text;

    public Message(boolean w, String t, Calendar s) {
        writtenByMe = w;
        text = t;
        timeWritten = s;
    }

    // Create an empty Message to fill with one of the initialise methods
    public Message() {
    }

    // getter functions

    public boolean isWrittenByMe() {
        return writtenByMe;
    }

    public boolean isAcked() {
        return acked;
    }

    public Calendar getTimeWritten() {
        return timeWritten;
    }

    public int getMyVectorClock() {
        return myVectorClock;
    }

    public int getTheirVectorClock() {
        return theirVectorClock;
    }

    public String getText() {
        return text;
    }

    // setter functions
    public void setWrittenByMe(boolean writtenByMe) {
        this.writtenByMe = writtenByMe;
    }

    public void setAcked(boolean acked) {
        this.acked = acked;
    }

    public void setTimeWritten(Calendar timeWritten) {
        this.timeWritten = timeWritten;
    }

    public void setMyVectorClock(int myVectorClock) {
        this.myVectorClock = myVectorClock;
    }

    public void setTheirVectorClock(int theirVectorClock) {
        this.theirVectorClock = theirVectorClock;
    }

    public void setText(String text) {
        this.text = text;
    }
}
