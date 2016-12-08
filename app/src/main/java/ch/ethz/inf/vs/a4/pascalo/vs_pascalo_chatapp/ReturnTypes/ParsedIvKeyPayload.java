package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes;

public class ParsedIvKeyPayload {
    // This contains:
    // 0 if everything worked
    // 1 if the JSON is malformed
    public int status;
    public byte[] iv;
    public byte[] key;
    public byte[] payload;
}
