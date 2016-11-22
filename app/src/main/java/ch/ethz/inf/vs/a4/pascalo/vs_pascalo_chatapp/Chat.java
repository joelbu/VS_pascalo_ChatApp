package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import java.util.LinkedList;
import java.util.UUID;


public class Chat {

    private UUID chatPatnerID;

    private String chatPartnerName;

    private String chatPartnerPublicKey;

    private LinkedList<Message> messageList;



    public Chat(UUID cPID, String cPN, String cPPK, LinkedList<Message> mL) {
        chatPatnerID = cPID;
        chatPartnerName = cPN;
        chatPartnerPublicKey = cPPK;
        messageList = mL;
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
    public UUID getChatPatnerID() {
        return chatPatnerID;
    }

    public String getChatPartnerName() {
        return chatPartnerName;
    }

    public String getChatPartnerPublicKey() {
        return chatPartnerPublicKey;
    }

    public LinkedList<Message> getMessageList() {
        return messageList;
    }


    // append a new message to the messageList
    public void addMessage(Message msg) {
        messageList.add(msg);
        // do we need to sort the functions after inserting to messageList?
                // I'm hoping we can just insert in the right position
        // messageList.sort( -----sorting function------- );
    }

}
