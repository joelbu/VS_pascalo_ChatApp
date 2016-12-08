package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.PublicKey;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.UUID;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Chat;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedChat;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedChatMap;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.VectorClock;

public class ChatParser {

    public static JSONObject serializeCollectionOfChats(Collection<Chat> chats) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (Chat chat : chats)
        {
            jsonArray.put(serializeSingleChat(chat));
        }
        try {
            jsonObject.put("chats", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject serializeSingleChat(Chat chat) {
        JSONObject json = new JSONObject();
        try {
            json.put("chatPartnerID", chat.getChatPatnerID().toString());
            json.put("chatPartnerName", chat.getChatPartnerName());

            if(chat.getChatPartnerPublicKey() == null) {
                json.put("chatPartnerKeyKnown", false);
            } else {
                json.put("chatPartnerKeyKnown", true);
                json.put("chatPartnerPublicKey", chat.getChatPartnerPublicKey());
            }
            json.put("unreadMessages", chat.getUnreadMessages());
            json.put("recentActivity", chat.getRecentActivity().getTimeInMillis());
            json.put("clockTime", chat.getLatestClock().serializeForStorage());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static ParsedChatMap parseMapOfChats(JSONObject jsonObject) {
        ParsedChatMap ret = new ParsedChatMap();

        try {
            JSONArray jsonArray = jsonObject.getJSONArray("chats");
            ret.chat = new HashMap<>(jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                ParsedChat parsedChat = parseSingleChat((JSONObject) jsonArray.get(i));
                if (parsedChat.status == 0) {
                    ret.chat.put(parsedChat.chat.getChatPatnerID(), parsedChat.chat);
                } else {
                    ret.status = 2;
                    break;
                }
            }

            ret.status = 0;
        } catch (JSONException e) {
            e.printStackTrace();
            ret.status = 1;
        }

        return ret;
    }

    public static ParsedChat parseSingleChat(JSONObject json) {
        ParsedChat ret = new ParsedChat();
        try {

            Calendar recentActivity = new GregorianCalendar();
            recentActivity.setTimeInMillis(json.getLong("recentActivity"));

            VectorClock clockTime = new VectorClock();
            clockTime.parseFromStorage(json.getJSONObject("clockTime"));

            PublicKey key = null;
            if(json.getBoolean("chatPartnerKeyKnown")) {
                key = KeyParser.parsePublicKey(json.getString("chatPartnerPublicKey"));
            }

            ret.chat = new Chat(
                    UUID.fromString(json.getString("chatPartnerID")),
                    json.getString("chatPartnerName"),
                    key,
                    json.getInt("unreadMessages"),
                    recentActivity,
                    clockTime
            );
            ret.status = 0;
        } catch (JSONException e) {
            e.printStackTrace();
            ret.status = 1;
        }
        return ret;
    }
}
