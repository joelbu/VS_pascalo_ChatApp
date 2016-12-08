package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes;

import java.util.UUID;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Message;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.VectorClock;

public class ParsedMessage {
    // This contains:
    // -1 if the JSON is malformed
    // 0 if this is a normal message
    // 1 if this is an ack message
    public int status;
    public Message message;
    public UUID sender;
}
