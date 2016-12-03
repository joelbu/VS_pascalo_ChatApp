package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes;

import java.util.TreeSet;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Message;

public class ParsedMessageThread {
    // This contains:
    // 0 if everything worked
    // 1 if the JSON is malformed
    // 2 if one of the included messages wasn't properly parsed
    public int status;
    public TreeSet<Message> messages;
}