package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Chat;

public class ParsedChat {
    // This contains:
    // 0 if everything worked
    // 1 if the JSON is malformed
    public int status;
    public Chat chat;
}
