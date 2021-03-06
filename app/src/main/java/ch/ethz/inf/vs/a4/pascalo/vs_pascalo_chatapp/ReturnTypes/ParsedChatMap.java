package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes;

import java.util.Map;
import java.util.UUID;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Chat;

public class ParsedChatMap {
    // This contains:
    // 0 if everything worked
    // 1 if the JSON is malformed
    // 2 if one of the included chats wasn't properly parsed
    public int status;
    public Map<UUID, Chat> chat;
}
