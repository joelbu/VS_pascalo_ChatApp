package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.UUID;


public class Chat {

    private UUID chatPatnerID;

    private String chatPartnerName;

    private String chatPartnerPublicKey;

    private LinkedList<Message> messageList;

    private int unreadMessages;

    private Calendar recentActivity;

    public Chat(UUID cPID, String cPN, String cPPK, LinkedList<Message> mL) {
        chatPatnerID = cPID;
        chatPartnerName = cPN;
        chatPartnerPublicKey = cPPK;
        messageList = mL;
        recentActivity = GregorianCalendar.getInstance();
        unreadMessages = 0;
    }

    public Chat(UUID cPID, String cPN, String cPPK, int uM, Calendar rA) {
        chatPatnerID = cPID;
        chatPartnerName = cPN;
        chatPartnerPublicKey = cPPK;
        unreadMessages = uM;
        recentActivity = rA;

        messageList = new LinkedList<>();
    }

    // setter for unread messages
    public void setUnreadMessages(int unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

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

    public Calendar getRecentActivity() { return recentActivity; }

    public int getUnreadMessages() { return unreadMessages; }




    public void updateRecentActivity() {
        recentActivity = GregorianCalendar.getInstance();
    }

    // append a new message to the messageList
    public void addMessage(Message msg) {
        messageList.add(msg);
        // do we need to sort the functions after inserting to messageList?
                // I'm hoping we can just insert in the right position
        // messageList.sort( -----sorting function------- );
    }

}
