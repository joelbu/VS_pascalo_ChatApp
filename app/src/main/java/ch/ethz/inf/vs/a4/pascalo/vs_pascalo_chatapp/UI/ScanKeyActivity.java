package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.UI;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.security.PublicKey;
import java.util.UUID;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ChatService;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers.KeyParser;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers.QRContentParser;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.R;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedQRContent;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ZXing.IntentIntegrator;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ZXing.IntentResult;

public class ScanKeyActivity extends AppCompatActivity{
    private ChatService mBoundService;
    private boolean mServiceIsBound;
    private UUID mChatPartnerID;
    EditText mNameEditText;
    EditText mKeyEditText;
    EditText mIdEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_key);

        Intent intent = getIntent();
        mChatPartnerID = (UUID) intent.getSerializableExtra("userid");

        mNameEditText = (EditText) findViewById(R.id.editText_partner_name);
        mKeyEditText = (EditText) findViewById(R.id.editText_public_key);
        mIdEditText = (EditText) findViewById(R.id.editText_partner_id);

        Log.d(ChatActivity.class.getSimpleName(), "binding Service");
        bindService(new Intent(getApplicationContext(),
                    ChatService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((ChatService.LocalBinder)service).getService();
            Log.d(ChatActivity.class.getSimpleName(), "Service bound");
            mServiceIsBound = true;

            // If we were called with an Id in the intent, that means we are supposed to show
            // what we know already so it can be changed
            if (mChatPartnerID != null) {
                mBoundService.setChatPartner(mChatPartnerID);
                mIdEditText.setText(mChatPartnerID.toString());
                mKeyEditText.setText(KeyParser.serializePublicKey(mBoundService.getPartnerKey()));
                mNameEditText.setText(mBoundService.getPartnerName());
            }


            Button scan = (Button) findViewById(R.id.button_scan_chat_partner_info);
            scan.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    IntentIntegrator integrator = new IntentIntegrator(ScanKeyActivity.this);
                    integrator.initiateScan();
                }
            });

            Button add = (Button) findViewById(R.id.button_add_chat_partner);
            add.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    PublicKey key = KeyParser.parsePublicKey(mKeyEditText.getText().toString());
                    if(key != null) {
                        mBoundService.addPartner(UUID.fromString(mIdEditText.getText().toString()),
                                mNameEditText.getText().toString(), key);
                        finish();
                    }
                }
            });
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            Log.d(ChatActivity.class.getSimpleName(), "Service unbound");
            mServiceIsBound = false;
        }
    };


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {

            String contents = scanResult.getContents();
            if (contents != null) {

                ParsedQRContent ret = QRContentParser.parse(contents);
                if (ret.status == 0) {
                    mNameEditText.setText(ret.name);
                    mKeyEditText.setText(ret.key);
                    mIdEditText.setText(ret.id.toString());
                } else {
                    mKeyEditText.setText(scanResult.getContents());
                    mNameEditText.setText("Parsing failed, status: " + ret.status);
                }

            } else {
                mNameEditText.setText("Scanning failed, scan Result: " + scanResult.toString());
            }

        } else {
            mNameEditText.setText("Scanning failed");
        }

    }

    @Override
    protected void onDestroy() {
        Log.d(ScanKeyActivity.class.getSimpleName(), "onDestroy() called");
        if (mServiceIsBound) {
            Log.d(ScanKeyActivity.class.getSimpleName(), "unbinding Service");
            unbindService(mConnection);
        }
        super.onDestroy();
    }
}
