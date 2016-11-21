package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Chat extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // read information out of intent

        // name of the chat partner

        // filename of chatfile

        // load chat messages out of file

    }

    @Override
    protected void onDestroy() {
        // save unsent messages with Tag "unsent" in chatfile

    }
}
