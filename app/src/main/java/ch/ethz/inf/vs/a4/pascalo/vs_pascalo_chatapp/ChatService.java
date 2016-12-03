package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import android.app.Activity;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Contacts;
import android.util.Log;

import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.UUID;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.UI.ShowKeyActivity;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers.QRContentParser;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ZXing.IntentIntegrator;

public class ChatService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ChatsHolder mChats;
    private Chat mCurrentChat;
    private boolean mChatsChanged;

    // For use in MainActivity only, ChatActivity is only supposed to interact with the current
    // chat through the methods below
    public Collection<Chat> getChats() {
        return mChats.getChats();
    }

    // Sets up the service to know what view is open in ChatActivity
    public void setChatPartner (UUID id) {
        mCurrentChat = mChats.getChat(id);
    }

    public LinkedList<Message> getMessages() {
        return mCurrentChat.getMessageList();
    }

    public String getPartnerName() {
        return mCurrentChat.getChatPartnerName();
    }

    public void forgetPartner() {
        mChats.forget(mCurrentChat.getChatPatnerID());
        mChatsChanged = true;
    }

    // Returns 0 on success, 1 for UUID already in use
    public int addPartner(UUID id, String name, String key) {
        int status = mChats.addPartner(id, name, key);
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
                mChats.getOwnId(),
                mChats.getOwnName(),
                mChats.getOwnPublicKey());
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
        mChats = new ChatsHolder();

        // Now it just loads everything upon starting, we still need lazy initialisation
        // TODO: Lazy initialisation of message threads
        mChats.readAddressBook(this);
        mChats.readAllThreads(this);

        // Since notifications must come from the service I moved this here
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .registerOnSharedPreferenceChangeListener(this);

        // TODO: Maybe read preferences into member fields

        // TODO: set chats to list

        UUID uuid = UUID.randomUUID();
        mChats.addPartner(uuid, "Hans Muster", "");

        UUID uuid1 = UUID.randomUUID();
        mChats.addPartner(uuid1, "Max Problem", "");

        mChats.addMessage(uuid, new Message(true, true, GregorianCalendar.getInstance(),
                new VectorClock(1, 4), "Text?"));
        mChats.addMessage(uuid, new Message(true, true, GregorianCalendar.getInstance(),
                new VectorClock(5, 5), "Text6"));
        mChats.addMessage(uuid, new Message(true, false, GregorianCalendar.getInstance(),
                new VectorClock(7, 6), "Text8"));
        mChats.addMessage(uuid, new Message(false, false, GregorianCalendar.getInstance(),
                new VectorClock(2, 2), "Text3"));
        mChats.addMessage(uuid, new Message(true, false, GregorianCalendar.getInstance(),
                new VectorClock(1, 0), "Text2a"));
        mChats.addMessage(uuid, new Message(false, false, GregorianCalendar.getInstance(),
                new VectorClock(0, 1), "Text2b"));
        mChats.addMessage(uuid, new Message(false, false, GregorianCalendar.getInstance(),
                new VectorClock(3, 4), "Text4"));
        mChats.addMessage(uuid, new Message(false, false, GregorianCalendar.getInstance(),
                new VectorClock(5, 6), "Text7"));
        mChats.addMessage(uuid, new Message(false, false, GregorianCalendar.getInstance(),
                new VectorClock(0, 0), "Text1"));



        mChats.addMessage(uuid1, new Message(false, false, GregorianCalendar.getInstance(),
                new VectorClock(0, 0), "test"));
        mChats.addMessage(uuid1, new Message(true, false, GregorianCalendar.getInstance(),
                new VectorClock(1, 1), "ack"));

        mChats.addMessage(uuid, new Message(false, false, GregorianCalendar.getInstance(),
                new VectorClock(0, 0), "Text1"));
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
        // TODO: destroy the service to go offline
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .unregisterOnSharedPreferenceChangeListener(this);

        // Writing upon service shutdown may not be needed if we write whenever
        // something new happens instead, but for now it will do
        mChats.writeAddressBook(this);
        mChats.writeAllThreads(this);

    }


}
