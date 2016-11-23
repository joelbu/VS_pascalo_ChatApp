package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

public class ChatBackgroundService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

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

        // Since notifications must come from the service I moved this here
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .registerOnSharedPreferenceChangeListener(this);

        // TODO: Maybe read preferences into member fields

        // TODO: set chats to list
        UUID uuid = UUID.randomUUID();
        Chat temp = new Chat(uuid, "Hans Muster", "", new LinkedList<Message>());
        mChats.put(uuid, temp);

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
    }


}
