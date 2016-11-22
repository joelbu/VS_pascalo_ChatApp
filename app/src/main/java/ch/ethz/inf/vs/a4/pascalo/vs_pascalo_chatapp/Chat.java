package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by pascal on 22.11.16.
 */

public class Chat {

    private String chatPartnerName;

    private String chatPartnerPublicKey;

    private LinkedList<Message> messageList;



    public Chat(String cPN, String cPPK, List<Message> mL) {
        chatPartnerName =cPN;
        chatPartnerPublicKey = cPPK;
    }

    /*
    // setter functions
    public void setChatPartnerName(String name) {
        chatPartnerName = name;
    }

    public void setChatPartnerPublicKey(String key) {
        chatPartnerPublicKey = key;
    }
    */



    // getter functions
    public String getChatPartnerName() {
        return chatPartnerName;
    }

    public String getChatPartnerPublicKey() {
        return chatPartnerPublicKey;
    }

    public List<Message> getMessageList() {
        return messageList;
    }


    // append a new message to the messageList
    public void appendMessageList(Message msg) {
        messageList.add(msg);
        // do we need to sort the functions after inserting to messageList?
        // messageList.sort( -----sorting function------- );
    }

}
