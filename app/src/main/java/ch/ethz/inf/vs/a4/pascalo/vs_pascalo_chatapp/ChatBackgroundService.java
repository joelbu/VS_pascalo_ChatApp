package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;

public class ChatBackgroundService extends Service {
    public ChatBackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onCreate() {
        // TODO: set flag that app knows that the service is running and does not start it again
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO:
    }

    public void onResume() {
        // TODO:
    }

    @Override
    public void onDestroy() {
        // TODO: destroy the service to go offline
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO:
    }


}
