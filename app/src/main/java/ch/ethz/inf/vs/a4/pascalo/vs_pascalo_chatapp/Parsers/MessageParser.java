package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Message;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedMessage;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedMessageThread;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.VectorClock;

public class MessageParser {

    // Made with randomness from atmospheric noise provided by random.org
    private static int magicNumber = -432185306;
    private UUID me;

    private MessageParser(UUID me) {
        this.me = me;
    }

    public static JSONObject serializeThreadForStorage(List<Message>messages) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            for(int i = 0; i < messages.size(); i++) {
                jsonArray.put(serializeForStorage(messages.get(i)));
            }
            jsonObject.put("thread", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    public static ParsedMessageThread parseThreadFromStorage(String string) {
        ParsedMessageThread ret = new ParsedMessageThread();
        try {
            JSONObject jsonObject = new JSONObject(string);
            JSONArray jsonArray = jsonObject.getJSONArray("thread");
            ret.messages = new LinkedList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                ParsedMessage parsedMessage = parseFromStorage((JSONObject) jsonArray.get(i));
                if (parsedMessage.status == 0) {
                    ret.messages.add(parsedMessage.message);
                } else {
                    ret.status = 2;
                    break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            ret.status = 1;
        }
        return ret;
    }

    // Serialize message to a JSON string, that is suitable for storage
    public static JSONObject serializeForStorage(Message message) {
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
    public static ParsedMessage parseFromStorage(JSONObject json) {
        ParsedMessage ret = new ParsedMessage();
        try {
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
        if (!string.substring(0, 25).contains(Integer.toString(magicNumber)))  {
            ret.status = 2;
            return ret;
        }

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
