package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

public class ChatBackgroundService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {


    String filename = "address_book";

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }

    public class LocalBinder extends Binder {
        ChatBackgroundService getService() {
            return ChatBackgroundService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        // TODO: Load address book from file etc

        FileInputStream inputStream;
        try {
            inputStream = openFileInput(filename);
            String address_bookIn = inputStream.toString();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Since notifications must come from the service I moved this here
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .registerOnSharedPreferenceChangeListener(this);

        // TODO: Maybe read preferences into member fields

        // TODO: set chats to list
        UUID uuid = UUID.randomUUID();
        Chat temp = new Chat(uuid, "Hans Muster", "", new LinkedList<Message>());

        UUID uuid1 = UUID.randomUUID();
        Chat temp1 = new Chat(uuid1, "Max Problem", "", new LinkedList<Message>());

        temp1.addMessage(new Message(false, "test", (GregorianCalendar) GregorianCalendar.getInstance()));
        temp1.addMessage(new Message(false, "ack", (GregorianCalendar) GregorianCalendar.getInstance()));

        mChats.put(uuid, temp);
        mChats.put(uuid1, temp1);

        addMessage(uuid, new Message(false, "test", (GregorianCalendar) GregorianCalendar.getInstance()));
        addMessage(uuid, new Message(false, "ack", (GregorianCalendar) GregorianCalendar.getInstance()));

        addMessage(uuid1, new Message(false, "test", (GregorianCalendar) GregorianCalendar.getInstance()));
        addMessage(uuid1, new Message(false, "ack", (GregorianCalendar) GregorianCalendar.getInstance()));
    }

    private HashMap<UUID, Chat> mChats = new HashMap<UUID, Chat>();

    public HashMap<UUID, Chat> getChats() { return mChats; }

    private void addMessage(UUID uuid, Message message) {
        mChats.get(uuid).addMessage(message);
        mChats.get(uuid).updateRecentActivity();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        // Sticky ensures that the Android system keeps the service around for us
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // TODO: destroy the service to go offline
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .unregisterOnSharedPreferenceChangeListener(this);


        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, this.getApplicationContext().MODE_PRIVATE);
            outputStream.write("Test".getBytes("UTF-8"));
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
