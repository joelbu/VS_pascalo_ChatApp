package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.UI;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ChatService;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.R;

public class GenerateKeyActivity extends AppCompatActivity {

    private ChatService mBoundService;
    private boolean mServiceIsBound;
    private Button mGenerateButton;
    private EditText mUsernameEditText;

    private static final String TAG = GenerateKeyActivity.class + "generating a key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_key);

        Log.d(GenerateKeyActivity.class.getSimpleName(), "binding Service");
        bindService(new Intent(getApplicationContext(),
                        ChatService.class), mConnection,
                Context.BIND_AUTO_CREATE);

        mGenerateButton = (Button) findViewById(R.id.button_generate_key);
        mUsernameEditText = (EditText) findViewById(R.id.editText_username);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((ChatService.LocalBinder)service).getService();
            Log.d(GenerateKeyActivity.class.getSimpleName(), "Service bound");
            mServiceIsBound = true;

            mGenerateButton.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View view) {
                    String username = mUsernameEditText.getText().toString();
                    if (!username.equals("")) {
                        try {
                            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                            Log.d(TAG, "KeyPairGenerator generated");
                            kpg.initialize(ChatService.RSA_KEY_LENGTH);
                            Log.d(TAG, "KeyPairGenerator initialized");
                            KeyPair kp = kpg.genKeyPair();
                            Log.d(TAG, "KeyPair generated");
                            PublicKey publicKey = kp.getPublic();
                            Log.d(TAG, "publicKey copied");
                            Log.d(TAG, "publicKey format: " + publicKey.getFormat());
                            PrivateKey privateKey = kp.getPrivate();
                            Log.d(TAG, "privateKey copied");
                            Log.d(TAG, "privateKey format: " + privateKey.getFormat());

                            mBoundService.setUpOwnInfo(UUID.randomUUID(), username, privateKey, publicKey);

                            Log.d(TAG, "username and keypair stored (chatservice) in addressbook");

                            // Setting the flag in the preferences, so on next start of the app we
                            // won't call GenerateKeyActivity again.
                            SharedPreferences prefs = PreferenceManager
                                    .getDefaultSharedPreferences(GenerateKeyActivity.this);
                            SharedPreferences.Editor edit = prefs.edit();
                            edit.putBoolean("is-key-pair-generated", true);
                            edit.apply();

                            mBoundService.generateTestChats();

                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d(TAG, "RSA key pair error");
                        }
                    } else {
                        Toast.makeText(
                                GenerateKeyActivity.this,
                                R.string.error_empty_username,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
            });
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Log.d(GenerateKeyActivity.class.getSimpleName(), "Service unbound");
            mServiceIsBound = false;
        }
    };

    @Override
    public void onDestroy() {
        Log.d(GenerateKeyActivity.class.getSimpleName(), "onDestroy() called");
        if (mServiceIsBound) {
            Log.d(GenerateKeyActivity.class.getSimpleName(), "unbinding Service");
            unbindService(mConnection);
        }
        super.onDestroy();
    }
}
