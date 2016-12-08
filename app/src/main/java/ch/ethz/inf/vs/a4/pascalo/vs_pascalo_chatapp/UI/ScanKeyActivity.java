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
import android.widget.Toast;

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

    String TAG = getClass().toString();

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
                Log.d(TAG, mBoundService.getPartnerName()
                        + " has key: " + mBoundService.getPartnerKey());
                // special case if we don't know the chatPartners key yet
                if (mBoundService.getPartnerKey() != null) {
                    mKeyEditText.setText(
                            KeyParser.serializePublicKey(
                                    mBoundService.getPartnerKey()
                            )
                    );
                }
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
                    try {
                        UUID chatPartnerId = UUID.fromString(mIdEditText.getText().toString());
                        String chatPartnerName = mNameEditText.getText().toString();
                        PublicKey key = KeyParser.parsePublicKey(mKeyEditText.getText().toString());
                        // TODO: can key be null or is it only empty ("")
                        if(key != null && !chatPartnerName.equals("")) {
                            mBoundService.addPartner(chatPartnerId, chatPartnerName, key);
                            Log.d(TAG, "The name of the chat partner is" + mNameEditText.getText().toString());
                            finish();
                        } else {
                            if (key == null) {
                                Toast.makeText(
                                        ScanKeyActivity.this,
                                        R.string.error_key_is_null,
                                        Toast.LENGTH_LONG
                                        ).show();
                            }
                            if (chatPartnerName.equals("")) {
                                Toast.makeText(
                                        ScanKeyActivity.this,
                                        R.string.error_chatPartnerName_is_empty,
                                        Toast.LENGTH_LONG
                                ).show();

                            }
                        }

                    } catch (IllegalArgumentException e) {
                        Toast.makeText(
                                ScanKeyActivity.this,
                                R.string.error_invalid_UUID,
                                Toast.LENGTH_LONG
                        ).show();
                        e.printStackTrace();
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
