package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import java.security.PublicKey;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.TreeSet;
import java.util.UUID;


public class Chat {

    private UUID chatPatnerID;

    private String chatPartnerName;

    private PublicKey chatPartnerPublicKey;

    private TreeSet<Message> messageList;

    private int unreadMessages;

    private Calendar recentActivity;

    private VectorClock latestClock;

    // Constructor used for adding new chat partners by user action
    public Chat(UUID cPID, String cPN, PublicKey cPPK) {
        chatPatnerID = cPID;
        chatPartnerName = cPN;
        chatPartnerPublicKey = cPPK;

        messageList = new TreeSet<>();
        unreadMessages = 0;
        recentActivity = GregorianCalendar.getInstance();
        latestClock = new VectorClock(0, 0);
    }

    // Constructor as used by the parser
    public Chat(UUID cPID, String cPN, PublicKey cPPK, int uM, Calendar rA, VectorClock vC) {
        chatPatnerID = cPID;
        chatPartnerName = cPN;
        chatPartnerPublicKey = cPPK;
        unreadMessages = uM;
        recentActivity = rA;
        latestClock = vC;
    }

    // setter for unread messages
    public void setUnreadMessages(int unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    public void setMessageList(TreeSet<Message> messageList) {
        this.messageList = messageList;
    }

    public void setChatPartnerName(String chatPartnerName) {
        this.chatPartnerName = chatPartnerName;
    }

    public void setChatPartnerPublicKey(PublicKey chatPartnerPublicKey) {
        this.chatPartnerPublicKey = chatPartnerPublicKey;
    }


    // getter functions
    public UUID getChatPatnerID() {
        return chatPatnerID;
    }

    public String getChatPartnerName() {
        return chatPartnerName;
    }

    public PublicKey getChatPartnerPublicKey() {
        return chatPartnerPublicKey;
    }

    public TreeSet<Message> getMessageList() {
        return messageList;
    }

    public Calendar getRecentActivity() { return recentActivity; }

    public int getUnreadMessages() { return unreadMessages; }

    public VectorClock getLatestClock() {
        return latestClock;
    }

    public boolean isKeyKnown() {
        return chatPartnerPublicKey != null;
    }

    public Message constructMessageFromUser(String text) {

        latestClock.tick();

        Message message = new Message(true, false, false, GregorianCalendar.getInstance(),
                new VectorClock(latestClock), text);

        messageList.add(message);
        updateRecentActivity();

        return message;
    }

    public void updateRecentActivity() {
        recentActivity = GregorianCalendar.getInstance();
    }

    // append a new message to the messageList
    public void addMessage(Message message) {
        unreadMessages = unreadMessages + 1;
        // The chat should always contain the latest point in time we have encountered, so we can
        // tick it once and append a clone of that to an outgoing message
        latestClock.setToMax(message.getClock());

        messageList.add(message);
        updateRecentActivity();
    }

    public void acknowledgeMessage(Message ack) {
        // Get least element in the tree greater than or equal to ack
        Message message = messageList.ceiling(ack);

        // Check to see if it's actually the correct message
        if (ack.equals(message)) {
            // Finally mark it acked
            message.setAcked(true);
        }
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
                return -1 * chat1.getRecentActivity().compareTo(chat2.getRecentActivity());
            }
        }
    };

}
