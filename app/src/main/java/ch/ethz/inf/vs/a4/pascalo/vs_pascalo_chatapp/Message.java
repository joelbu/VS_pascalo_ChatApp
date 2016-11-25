package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

public class Message {

    private boolean writtenByMe;
    private boolean acked;

    // For user display
    private Calendar timeWritten;

    // For ordering and acking
    private VectorClock clock;

    // Actual text written by user
    private String text;

    // Used by serializer and for testing
    public Message(boolean writtenByMe, boolean acked, Calendar timeWritten,
                   VectorClock clock, String text) {

        this.writtenByMe = writtenByMe;
        this.acked = acked;
        this.timeWritten = timeWritten;
        this.clock = clock;
        this.text = text;

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

    public VectorClock getClock() {
        return clock;
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

    public void setClock(VectorClock clock) {
        this.clock = clock;
    }

    public void setText(String text) {
        this.text = text;
    }
}
