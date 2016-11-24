package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

public class MessageParserSerializer {

    private UUID me;
    private int magicNumber;

    private MessageParserSerializer(UUID me, int magicNumber) {
        this.me = me;
        this.magicNumber = magicNumber;
    }

    // Serialize message to a JSON string, that is suitable for storage
    public String serializeForStorage(Message message) {
        JSONObject json = new JSONObject();
        try {
            json.put("writtenByMe", message.isWrittenByMe());
            json.put("acked", message.isAcked());

            json.put("timeWritten", message.getTimeWritten().getTimeInMillis());

            json.put("clock", message.getClock().serializeForStorage());

            json.put("text", message.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    // Initialise message from a JSON string, that came from storage
    public Message parseFromStorage(String string) {
        Message message = new Message();
        try {
            JSONObject json = new JSONObject(string);
            message = new Message();

            message.setWrittenByMe(json.getBoolean("writtenByMe"));
            message.setAcked(json.getBoolean("acked"));

            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(json.getLong("timeWritten"));
            message.setTimeWritten(calendar);

            VectorClock clock = new VectorClock();
            clock.parseFromStorage(json.getString("clock"));
            message.setClock(clock);

            message.setText(json.getString("text"));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return message;
    }

    // Initialise message from a JSON string, that came from the network
    public String serializeForNetwork(Message message) {
        JSONObject json = new JSONObject();
        try {
            json.put("magic", magicNumber);

            json.put("timeWritten", message.getTimeWritten().getTimeInMillis());
            json.put("clock", message.getClock().serializeForNetwork());

            json.put("sender", me.toString());
            json.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    // Parse given String, that came from network into given message and sender.
    public MessageParserReturnTriple parseFromNetwork(String string) {
        MessageParserReturnTriple ret = new MessageParserReturnTriple();

        // The magic number must be early in the string, if not don't bother trying JSON
        if (!string.substring(0, 20).contains(Integer.toString(magicNumber))) ret.status = 1;

        try {
            ret.message = new Message();
            
            //This information is not contained in the network message, but clear from circumstance
            ret.message.setWrittenByMe(false);
            ret.message.setAcked(false);

            JSONObject json = new JSONObject(string);

            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(json.getLong("timeWritten"));
            ret.message.setTimeWritten(calendar);

            VectorClock clock = new VectorClock();
            clock.parseFromNetwork(json.getString("clock"));
            ret.message.setClock(clock);

            ret.sender = UUID.fromString(json.getString("sender"));
            ret.message.setText(json.getString("text"));
        } catch (JSONException e) {
            e.printStackTrace();
            ret.status = 2;
        }
        ret.status = 0;
        return ret;
    }


}
