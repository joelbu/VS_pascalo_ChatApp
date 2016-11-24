package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

public class MessageParserSerializer {

    private UUID mMe;
    private int mMagicNumber;

    private MessageParserSerializer(UUID me, int magicNumber) {
        mMe = me;
        mMagicNumber = magicNumber;
    }

    // Initialise Message from a JSON String, that came from storage
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

            message.setMyVectorClock(json.getInt("myVectorClock"));
            message.setTheirVectorClock(json.getInt("theirVectorClock"));

            message.setText(json.getString("text"));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return message;
    }

    public JSONObject serializeForStorage(Message message) {
        JSONObject json = new JSONObject();
        try {
            json.put("writtenByMe", message.isWrittenByMe());
            json.put("acked", message.isAcked());

            json.put("timeWritten", message.getTimeWritten().getTimeInMillis());

            json.put("myVectorClock", message.getMyVectorClock());
            json.put("theirVectorClock", message.getTheirVectorClock());

            json.put("text", message.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public JSONObject serializeForNetwork(Message message) {
        JSONObject json = new JSONObject();
        try {
            json.put("magic", mMagicNumber);

            json.put("timeWritten", message.getTimeWritten().getTimeInMillis());
            json.put("senderClock", message.getMyVectorClock()); // I'm sender
            json.put("receiverClock", message.getTheirVectorClock()); // They are receiver

            json.put("sender", mMe.toString());
            json.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    // Parse given String, that came from network into given Message and sender.
    public MessageParserReturnTriple parseFromNetwork(String string) {
        MessageParserReturnTriple ret = new MessageParserReturnTriple();

        // The magic number must be early in the string, if not don't bother trying JSON
        if (!string.substring(0, 20).contains(Integer.toString(mMagicNumber))) ret.status = 1;

        try {
            ret.message = new Message();
            
            //This information is not contained in the network message, but clear from circumstance
            ret.message.setWrittenByMe(false);
            ret.message.setAcked(false);

            JSONObject json = new JSONObject(string);

            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(json.getLong("timeWritten"));
            ret.message.setTimeWritten(calendar);

            ret.message.setTheirVectorClock(json.getInt("senderClock")); // They are sender
            ret.message.setMyVectorClock(json.getInt("receiverClock")); // I'm receiver

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
