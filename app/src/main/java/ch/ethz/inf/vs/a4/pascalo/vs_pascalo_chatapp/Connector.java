package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;


import android.content.Context;
import android.util.Log;

import java.util.Arrays;

public class Connector extends WDMF_Connector {
    private final ChatService mService;
    private final Context mContext;

    public Connector(Context c, String applicationTag, ChatService service){
        super(c, applicationTag);
        mService = service;
        mContext = c;
    }

    @Override
    public void onReceiveMessage(byte[] message) {
        mService.onReceiveMessage(message);
    }

    @Override
    public boolean broadcastMessage(byte[] data) {
        Log.d(this.getClass().getSimpleName(), "Raw message: " + Arrays.toString(data));
        return super.broadcastMessage(data);
    }
}
