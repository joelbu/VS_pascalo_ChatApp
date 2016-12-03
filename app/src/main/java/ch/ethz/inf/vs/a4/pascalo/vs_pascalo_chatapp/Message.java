package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;


import java.util.Calendar;
import java.util.Comparator;

public class Message implements Comparable {

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

    @Override
    public int compareTo(Object o) {
        Message message1 = this;
        Message message2 = (Message) o;

        // Do normal vector clock comparison...
        int comparison = message1.getClock().compareToClock(message2.getClock());
        if (comparison != 0) {
            return comparison;
        } else {
            // ...and assign an arbitrary order among incomparables
            return message1.getClock().totalOrder(message2.getClock());
        }
    }

    @Override
    public boolean equals (Object obj) {
        return this.clock.equals(((Message)obj).clock);
    }

    public static final Comparator<Message> COMPARATOR = new Comparator<Message>() {
        @Override
        public int compare(Message message1, Message message2) {
            return message1.compareTo(message2);
        }
    };
}
