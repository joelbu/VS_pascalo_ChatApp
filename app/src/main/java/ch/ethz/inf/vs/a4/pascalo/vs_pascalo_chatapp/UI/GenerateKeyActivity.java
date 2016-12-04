package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.UI;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.R;

public class GenerateKeyActivity extends AppCompatActivity implements OnClickListener {

    String TAG = this.getClass() + "generating a key";

    Key publicKey = null;
    Key privateKey = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_key);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_generate_key:
                try {
                    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                    Log.d(TAG, "KeyPaitGenerator generated");
                    kpg.initialize(16);
                    Log.d(TAG, "KeyPairGenerator initialized");
                    KeyPair kp = kpg.genKeyPair();
                    Log.d(TAG, "KeyPair generated");
                    publicKey = kp.getPublic();
                    Log.d(TAG, "publicKey copied");
                    privateKey = kp.getPrivate();
                    Log.d(TAG, "privateKey copied");
                } catch (Exception e) {
                    Log.d(TAG, "RSA key pair error");
                }
                break;
            default:

        }
    }
}
