package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes;

public class ParsedQRContent {
    // This contains:
    // 0 if everything worked
    // 1 if the JSON is malformed
    // 2 if there is no magic number and therefore decryption must have failed
    public int status;
    public String name;
    public String key;
}
