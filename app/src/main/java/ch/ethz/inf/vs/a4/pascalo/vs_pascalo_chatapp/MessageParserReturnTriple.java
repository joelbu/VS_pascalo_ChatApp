package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import java.util.UUID;

public class MessageParserReturnTriple {
    // This contains:
    // 0 if everything worked
    // 1 if there is no magic number and therefore decryption must have failed
    // 2 if the JSON is malformed
    public int status;
    public Message message;
    public UUID sender;
}
