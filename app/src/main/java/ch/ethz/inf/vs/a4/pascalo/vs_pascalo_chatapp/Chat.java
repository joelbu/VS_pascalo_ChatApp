package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import java.util.List;

/**
 * Created by pascal on 22.11.16.
 */

public class Chat {

    private String chatPartnerName;

    private String chatPartnerPublicKey;

    private List<Message> messageList;


    // setter functions
    public void setChatPartnerName(String name) {
        chatPartnerName = name;
    }

    public void setChatPartnerPublicKey(String key) {
        chatPartnerPublicKey = key;
    }


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
