package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;


import android.content.Context;

public class Connector extends WDMF_Connector {
    private final ChatService mService;

    public Connector(Context c, String applicationTag, ChatService service){
        super(c, applicationTag);
        mService = service;
    }

    @Override
    public void onReceiveMessage(byte[] message) {
        mService.onReceiveMessage(message);
    }
}
