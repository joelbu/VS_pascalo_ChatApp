package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TreeSet;
import java.util.UUID;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Message;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedMessage;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedMessageThread;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.VectorClock;

public class MessageParser {

    private UUID me;

    private String TAG = "MessageParser";

    public MessageParser(UUID me) {
        this.me = me;
    }

    public static JSONObject serializeThreadForStorage(TreeSet<Message> messages) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            for(Message message: messages) {
                jsonArray.put(serializeForStorage(message));
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
            ret.messages = new TreeSet<>();

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
            clock.parseFromStorage(json.getJSONObject("clock"));

            ret.message = new Message(
                    json.getBoolean("writtenByMe"),
                    json.getBoolean("acked"),
                    false,
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
            if (message.isAckMessage()) {
                json.put("isAck", true);
                json.put("sender", me.toString());
                json.put("clock", message.getClock().serializeForNetwork());

            } else {
                json.put("isAck", false);
                json.put("sender", me.toString());
                json.put("clock", message.getClock().serializeForNetwork());
                json.put("timeWritten", message.getTimeWritten().getTimeInMillis());
                json.put("text", message.getText());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    // Parse given String, that came from network into given message and sender.
    public ParsedMessage parseFromNetwork(String string) {
        ParsedMessage ret = new ParsedMessage();

        try {
            JSONObject json = new JSONObject(string);

            if (json.getBoolean("isAck")) {

                ret.status = 1;
                ret.sender = UUID.fromString(json.getString("sender"));

                VectorClock clock = new VectorClock();
                clock.parseFromNetwork(json.getJSONObject("clock"));
                ret.message = new Message(
                        //This information is not contained in the network message, but clear from circumstance
                        false,
                        true,
                        true,
                        null,
                        clock,
                        null
                );

            } else {

                ret.status = 0;
                ret.sender = UUID.fromString(json.getString("sender"));

                VectorClock clock = new VectorClock();
                clock.parseFromNetwork(json.getJSONObject("clock"));
                Calendar calendar = new GregorianCalendar();
                calendar.setTimeInMillis(json.getLong("timeWritten"));
                String text = json.getString("text");

                ret.message = new Message(
                        //This information is not contained in the network message, but clear from circumstance
                        false,
                        false,
                        false,
                        calendar,
                        clock,
                        text
                );

                ret.sender = UUID.fromString(json.getString("sender"));

            }

        } catch (JSONException e) {
            e.printStackTrace();
            ret.status = -1;
        }
        return ret;
    }


}
