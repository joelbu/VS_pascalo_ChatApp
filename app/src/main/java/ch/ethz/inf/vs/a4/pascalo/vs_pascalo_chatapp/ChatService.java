package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import android.app.Activity;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.TreeSet;
import java.util.UUID;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers.MessageParser;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedMessage;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.UI.ShowKeyActivity;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers.QRContentParser;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ZXing.IntentIntegrator;

public class ChatService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ChatsHolder mChatsHolder;
    private Chat mCurrentChat;
    private boolean mChatsChanged;
    private Connector mConnector;
    private MessageParser mMessageParser;

    // For use in MainActivity only, ChatActivity is only supposed to interact with the current
    // chat through the methods below
    public Collection<Chat> getChats() {
        return mChatsHolder.getChats();
    }

    // Sets up the service to know what view is open in ChatActivity
    public void setChatPartner (UUID id) {
        mCurrentChat = mChatsHolder.getChat(id);
    }

    public TreeSet<Message> getMessages() {
        return mCurrentChat.getMessageList();
    }

    public String getPartnerName() {
        return mCurrentChat.getChatPartnerName();
    }

    public void forgetPartner() {
        mChatsHolder.forget(mCurrentChat.getChatPatnerID());
        mChatsChanged = true;
    }

    // Returns 0 on success, 1 for UUID already in use
    public int addPartner(UUID id, String name, String key) {
        int status = mChatsHolder.addPartner(id, name, key);
        if (status == 0) {
            mChatsChanged = true;
        }
        return status;
    }

    public boolean getChatsChanged() {
        return mChatsChanged;
    }

    public void resetChatsChanged() {
        mChatsChanged = false;
    }

    public void setUnreadMessages(int unreadMessages) {
        mCurrentChat.setUnreadMessages(unreadMessages);
    }

    public void shareMyInfo(Activity activity) {
        shareInfo(activity,
                mChatsHolder.getOwnId(),
                mChatsHolder.getOwnName(),
                mChatsHolder.getOwnPublicKey());
    }

    public void shareChatPartnerInfo(Activity activity) {
        shareInfo(activity,
                mCurrentChat.getChatPatnerID(),
                mCurrentChat.getChatPartnerName(),
                mCurrentChat.getChatPartnerPublicKey());
    }

    private void shareInfo(final Activity activity, UUID id, String name, String key) {
        final String info = QRContentParser.serialize(id, name, key).toString();
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.shareText(info, "TEXT_TYPE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Intent intent = new Intent(activity, ShowKeyActivity.class);
                intent.putExtra("info", info);
                activity.startActivity(intent);

            }
        });
    }

    // This is intended to be called from ChatActivity after a user pressed send
    public void sendMessage(String text) {

        // The Chat has the context information necessary to construct a Message
        Message message = mCurrentChat.constructMessageFromUser(text);

        // Telling the UI that something has changed
        broadcastViewChange();

        String networkString = mMessageParser.serializeForNetwork(message).toString();

        //TODO: Encrypt message

        try {
            mConnector.broadcastMessage(networkString.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // This should never happen, because UTF-8 is always supported
            e.printStackTrace();
        }

    }

    private void broadcastViewChange() {
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(new Intent("UPDATE_MESSAGE_VIEW"));
    }

    public void onReceiveMessage(byte[] message) {
        try {
            String networkString = new String(message, "UTF-8");

            // TODO: Decrypt message

            ParsedMessage ret = mMessageParser.parseFromNetwork(networkString);

            mChatsHolder.addMessage(ret.sender, ret.message);

            if (ret.sender.equals(mCurrentChat.getChatPatnerID())) {
                broadcastViewChange();
            }

        } catch (UnsupportedEncodingException e) {
            // This should never happen, because UTF-8 is always supported
            e.printStackTrace();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // TODO: Make sure we react to configuration changes like vibration off
    }

    public class LocalBinder extends Binder {
        public ChatService getService() {
            return ChatService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        mChatsHolder = new ChatsHolder();

        // Now it just loads everything upon starting, we still need lazy initialisation
        // TODO: Lazy initialisation of message threads
        mChatsHolder.readAddressBook(this);
        mChatsHolder.readAllThreads(this);

        // Since notifications must come from the service I moved this here
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .registerOnSharedPreferenceChangeListener(this);

        // TODO: Maybe read preferences into member fields

        // TODO: set chats to list

        UUID uuid = UUID.randomUUID();
        mChatsHolder.addPartner(uuid, "Hans Muster", "");

        UUID uuid1 = UUID.randomUUID();
        mChatsHolder.addPartner(uuid1, "Max Problem", "");

        mChatsHolder.addMessage(uuid, new Message(true, false, GregorianCalendar.getInstance(),
                new VectorClock(1, 4), "Text?"));
        mChatsHolder.addMessage(uuid, new Message(true, false, GregorianCalendar.getInstance(),
                new VectorClock(5, 5), "Text6"));
        mChatsHolder.addMessage(uuid, new Message(true, false, GregorianCalendar.getInstance(),
                new VectorClock(7, 6), "Text8"));
        mChatsHolder.addMessage(uuid, new Message(false, true, GregorianCalendar.getInstance(),
                new VectorClock(2, 2), "Text3"));
        mChatsHolder.addMessage(uuid, new Message(true, true, GregorianCalendar.getInstance(),
                new VectorClock(1, 0), "Text2a"));
        mChatsHolder.addMessage(uuid, new Message(false, true, GregorianCalendar.getInstance(),
                new VectorClock(0, 1), "Text2b"));
        mChatsHolder.addMessage(uuid, new Message(false, true, GregorianCalendar.getInstance(),
                new VectorClock(3, 4), "Text4"));
        mChatsHolder.addMessage(uuid, new Message(false, true, GregorianCalendar.getInstance(),
                new VectorClock(5, 6), "Text7"));
        mChatsHolder.addMessage(uuid, new Message(false, true, GregorianCalendar.getInstance(),
                new VectorClock(0, 0), "Text1"));



        mChatsHolder.addMessage(uuid1, new Message(false, false, GregorianCalendar.getInstance(),
                new VectorClock(0, 1), "test"));
        mChatsHolder.addMessage(uuid1, new Message(true, false, GregorianCalendar.getInstance(),
                new VectorClock(1, 1), "ack"));

        mMessageParser = new MessageParser(mChatsHolder.getOwnId());

        mConnector = new Connector(this, "SuperCoolPrivateChattingApp", this);
        mConnector.connectToWDMF();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        // Sticky ensures that the Android system keeps the service around for us
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(ChatService.class.getSimpleName(), "onDestroy() called");
        mConnector.disconnectFromWDMF();
        // TODO: destroy the service to go offline
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .unregisterOnSharedPreferenceChangeListener(this);

        // Writing upon service shutdown may not be needed if we write whenever
        // something new happens instead, but for now it will do
        mChatsHolder.writeAddressBook(this);
        mChatsHolder.writeAllThreads(this);

    }


}
