package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.UUID;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.UI.MainActivity;

public class ChatService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ChatsHolder mChats;
    private Chat mCurrentChat;

    // For use in MainActivity only, ChatActivity is only supposed to interact with the current
    // chat through the methods below
    public Collection<Chat> getChats() {
        return mChats.getChats();
    }

    public void setChatPartner (UUID id) {
        mCurrentChat = mChats.getChat(id);
    }

    public LinkedList<Message> getMessages() {
        return mCurrentChat.getMessageList();
    }

    public String getPartnerName() {
        return mCurrentChat.getChatPartnerName();
    }

    public void setUnreadMessages(int unreadMessages) {
        mCurrentChat.setUnreadMessages(unreadMessages);
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
