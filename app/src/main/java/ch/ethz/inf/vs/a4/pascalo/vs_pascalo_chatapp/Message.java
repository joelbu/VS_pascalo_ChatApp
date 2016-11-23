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

    // Only populated during parsing
    private UUID sender;

    private String message;

    public Message(boolean w, String m, Calendar s) {
        writtenByMe = w;
        message = m;
        timeWritten = s;
    }

    public void setAcked() {
        acked = true;
    }


    // getter functions
    public boolean getWrittenByMe() {
        return writtenByMe;
    }

    public String getMessage() {
        return message;
    }

    public Calendar getTimeSent() {
        return timeWritten;
    }


    public boolean isAcked() {
        return acked;
    }

    public JSONObject getJsonForStorage() {
        JSONObject json = new JSONObject();
        try {
            json.put("writtenByMe", writtenByMe);
            json.put("acked", acked);
            json.put("timeWritten", timeWritten.getTimeInMillis());
            json.put("myVectorClock", myVectorClock);
            json.put("theirVectorClock", theirVectorClock);
            json.put("message", message);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }



    // Create an empty Message to fill with one of the initialise methods
    public Message(String string, boolean fromStorage) {
    }

    // Initialise an empty Message from a JSON String, that came from storage
    public void initialiseFromStorage(String string) {
        try {
            JSONObject json = new JSONObject(string);

            writtenByMe = json.getBoolean("writtenByMe");
            acked = json.getBoolean("acked");

            timeWritten = new GregorianCalendar();
            timeWritten.setTimeInMillis(json.getLong("timeWritten"));
            myVectorClock = json.getInt("myVectorClock");
            theirVectorClock = json.getInt("theirVectorClock");

            message = json.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Initialise an empty Message from a JSON String, that came from network. It returns:
    // 0 if everything has worked,
    // 1 if this message was not successfully decrypted
    // 2 if the message was decrypted but is malformed
    public int initialiseFromNetwork(String string, int magicNumber) {
        // The magic number must be early in the string, if not don't bother trying JSON
        if (!string.substring(0, 20).contains(Integer.toString(magicNumber))) return 1;

        try {
            //This information is not contained in the network message, but clear from circumstance
            writtenByMe = false;
            acked = false;

            JSONObject json = new JSONObject(string);

            timeWritten = new GregorianCalendar();
            timeWritten.setTimeInMillis(json.getLong("timeWritten"));
            theirVectorClock = json.getInt("senderClock"); // They are sender
            myVectorClock = json.getInt("receiverClock"); // I'm receiver

            sender = UUID.fromString(json.getString("sender"));
            message = json.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
            return 2;
        }
        return 0;
    }

    public JSONObject getJsonForNetwork(UUID me, int magicNumber) {
        JSONObject json = new JSONObject();
        try {
            json.put("magic", magicNumber);

            json.put("timeWritten", timeWritten.getTimeInMillis());
            json.put("senderClock", myVectorClock); // I'm sender
            json.put("receiverClock", theirVectorClock); // They are receiver

            json.put("sender", me.toString());
            json.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

}
