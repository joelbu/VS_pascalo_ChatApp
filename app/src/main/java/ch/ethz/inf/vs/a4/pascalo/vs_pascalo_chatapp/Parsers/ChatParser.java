package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Chat;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedChat;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedChatMap;

public class ChatParser {

    public JSONObject serializeMapOfChats(Map<UUID,Chat> chats) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (Chat chat : chats.values())
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

    public JSONObject serializeSingleChat(Chat chat) {
        JSONObject json = new JSONObject();
        try {
            json.put("chatPartnerID", chat.getChatPatnerID().toString());
            json.put("chatPartnerName", chat.getChatPartnerName());
            json.put("chatPartnerPublicKey", chat.getChatPartnerPublicKey());
            json.put("unreadMessages", chat.getUnreadMessages());
            json.put("recentActivity", chat.getRecentActivity().getTimeInMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public ParsedChatMap parseMapOfChats(String string) {
        ParsedChatMap ret = new ParsedChatMap();

        try {
            JSONObject jsonObjet = new JSONObject(string);
            JSONArray jsonArray = jsonObjet.getJSONArray("chats");
            ret.chat = new HashMap<>(jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                ParsedChat parsedChat = parseSingleChat((String) jsonArray.get(i));
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

    public ParsedChat parseSingleChat(String string) {
        ParsedChat ret = new ParsedChat();
        try {
            JSONObject json = new JSONObject(string);

            Calendar recentActivity = new GregorianCalendar();
            recentActivity.setTimeInMillis(json.getLong("recentActivity"));

            ret.chat = new Chat(
                    UUID.fromString(json.getString("chatPartnerID")),
                    json.getString("chatPartnerName"),
                    json.getString("chatPartnerPublicKey"),
                    json.getInt("unreadMessages"),
                    recentActivity
            );
            ret.status = 0;
        } catch (JSONException e) {
            e.printStackTrace();
            ret.status = 1;
        }
        return ret;
    }
}
