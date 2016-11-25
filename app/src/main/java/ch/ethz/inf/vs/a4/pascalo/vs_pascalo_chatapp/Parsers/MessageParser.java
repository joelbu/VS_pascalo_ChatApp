package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Message;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedMessage;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.VectorClock;

public class MessageParser {

    private UUID me;
    private int magicNumber;

    private MessageParser(UUID me, int magicNumber) {
        this.me = me;
        this.magicNumber = magicNumber;
    }

    // Serialize message to a JSON string, that is suitable for storage
    public JSONObject serializeForStorage(Message message) {
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
        return json;
    }

    // Initialise message from a JSON string, that came from storage
    public ParsedMessage parseFromStorage(String string) {
        ParsedMessage ret = new ParsedMessage();
        try {
            JSONObject json = new JSONObject(string);

            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(json.getLong("timeWritten"));

            VectorClock clock = new VectorClock();
            clock.parseFromStorage(json.getString("clock"));

            ret.message = new Message(
                    json.getBoolean("writtenByMe"),
                    json.getBoolean("acked"),
                    calendar,
                    clock,
                    json.getString("text")
            );

            ret.status = 0;
        } catch (JSONException e) {
            e.printStackTrace();
            ret.status = 1;
        }
        return ret;
    }








    // Initialise message from a JSON string, that came from the network
    public JSONObject serializeForNetwork(Message message) {
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
        return json;
    }

    // Parse given String, that came from network into given message and sender.
    public ParsedMessage parseFromNetwork(String string) {
        ParsedMessage ret = new ParsedMessage();

        // The magic number must be early in the string, if not don't bother trying JSON
        if (!string.substring(0, 20).contains(Integer.toString(magicNumber))) ret.status = 2;

        try {
            JSONObject json = new JSONObject(string);

            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(json.getLong("timeWritten"));

            VectorClock clock = new VectorClock();
            clock.parseFromNetwork(json.getString("clock"));

            ret.message = new Message(
                    //This information is not contained in the network message, but clear from circumstance
                    false,
                    false,
                    calendar,
                    clock,
                    json.getString("text")
            );

            ret.sender = UUID.fromString(json.getString("sender"));
            ret.status = 0;
        } catch (JSONException e) {
            e.printStackTrace();
            ret.status = 1;
        }
        return ret;
    }


}
