package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.TreeSet;
import java.util.UUID;


public class Chat {

    private UUID chatPatnerID;

    private String chatPartnerName;

    private String chatPartnerPublicKey;

    private TreeSet<Message> messageList;

    private int unreadMessages;

    private Calendar recentActivity;

    // Constructor used for adding new chat partners by user action
    public Chat(UUID cPID, String cPN, String cPPK) {
        chatPatnerID = cPID;
        chatPartnerName = cPN;
        chatPartnerPublicKey = cPPK;

        messageList = new TreeSet<>();
        recentActivity = GregorianCalendar.getInstance();
        unreadMessages = 0;
    }

    // Constructor as used by the parser
    public Chat(UUID cPID, String cPN, String cPPK, int uM, Calendar rA) {
        chatPatnerID = cPID;
        chatPartnerName = cPN;
        chatPartnerPublicKey = cPPK;
        unreadMessages = uM;
        recentActivity = rA;
    }

    // setter for unread messages
    public void setUnreadMessages(int unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    public void setMessageList(TreeSet<Message> messageList) {
        this.messageList = messageList;
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

    public TreeSet<Message> getMessageList() {
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
    }

    public static final Comparator<Chat> COMPARATOR = new Comparator<Chat>() {

        @Override
        public int compare(Chat chat1, Chat chat2) {
            // All chats with unread messages need to be above all those without
            // Within the two categories order by recent activity
            // This feels more natural and useful than just ordering by recent activity
            if (chat1.getUnreadMessages() == 0 && chat2.getUnreadMessages() > 0) {
                return 1;
            } else if (chat2.getUnreadMessages() == 0 && chat1.getUnreadMessages() > 0) {
                return -1;
            } else {
                return chat1.getRecentActivity().compareTo(chat2.getRecentActivity());
            }
        }
    };

}
