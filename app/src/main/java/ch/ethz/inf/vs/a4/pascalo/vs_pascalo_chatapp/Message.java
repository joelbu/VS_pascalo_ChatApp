package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Message {

    private boolean writtenByMe;

    private String message;

    private Calendar timeSent;

    private boolean acked = false;

    public Message(boolean w, String m, Calendar s) {
        writtenByMe = w;
        message = m;
        timeSent = s;
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
        return timeSent;
    }


    public boolean isAcked() {
        return acked;
    }

    public JSONObject getJsonRepresentationForStorage() {
        JSONObject json = new JSONObject();
        try {
            json.put("writtenByMe", writtenByMe);
            json.put("acked", acked);
            json.put("message", message);
            json.put("timeSent", timeSent.getTimeInMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    // Create a Message from the JSON representation, if it comes from the storage we have all fields
    public Message(String string, boolean fromStorage) {
        try {
            JSONObject json = new JSONObject(string);
            writtenByMe = json.getBoolean("writtenByMe");
            acked = json.getBoolean("acked");
            message = json.getString("message");
            timeSent = new GregorianCalendar();
            timeSent.setTimeInMillis(json.getLong("timeSent"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
