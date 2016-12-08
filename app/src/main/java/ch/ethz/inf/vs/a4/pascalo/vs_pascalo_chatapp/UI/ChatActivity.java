package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.UI;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.TreeSet;
import java.util.UUID;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ChatService;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Message;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.R;

public class ChatActivity extends AppCompatActivity{

    private ChatService mBoundService;
    private ArrayAdapter<Message> mMessageArrayAdapter;
    private UUID mChatPartnerID;
    private boolean mServiceIsBound;
    private ListView mMessageListView;
    private Button mScanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // overflow menu
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);

        Intent intent = getIntent();
        mChatPartnerID = (UUID) intent.getSerializableExtra("userid");

        Log.d(ChatActivity.class.getSimpleName(), "Chat partner is: " +
                mChatPartnerID.toString());

        Log.d(ChatActivity.class.getSimpleName(), "binding Service");
        bindService(new Intent(getApplicationContext(),
                        ChatService.class), mConnection,
                Context.BIND_AUTO_CREATE);

        mScanButton = (Button) findViewById(R.id.button_send_messege);

        // register listener on chatList
        ListView messageListView = (ListView) findViewById(R.id.messageList);
        messageListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Message message = (Message) parent.getItemAtPosition(position);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Message", message.getText());
                clipboard.setPrimaryClip(clip);
                return true;
            }
        });
    }

    @Override
    protected void onRestart() {
        // Set the ActivityTitle to the name
        getSupportActionBar().setTitle(mBoundService.getPartnerName());

        // Only enable sending if we know their key, which is not guaranteed
        mScanButton.setEnabled(mBoundService.isKeyKnown());
        super.onRestart();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((ChatService.LocalBinder)service).getService();
            Log.d(ChatActivity.class.getSimpleName(), "Service bound");
            mServiceIsBound = true;

            // Tell the user about this for our demo.
            Toast.makeText(ChatActivity.this, R.string.local_service_connected,
                    Toast.LENGTH_SHORT).show();

            // Tell the service who the current partner is, so it knows on which chats to
            // call the functions
            Log.d(ChatActivity.class.getSimpleName(), "Setting chat partner");
            mBoundService.setChatPartner(mChatPartnerID);

            // TODO: and only enable if our key is correctly generated
            // but this should never happen because the id has to be generated at the first start of app

            // Only enable sending if we know their key, which is not guaranteed
            mScanButton.setEnabled(mBoundService.isKeyKnown());

            Button scan = (Button) findViewById(R.id.button_send_messege);
            scan.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EditText editText = (EditText) findViewById(R.id.editMessage);
                    String msg = editText.getText().toString();
                    if (!msg.equals("")) {
                        mBoundService.sendMessage(msg);
                        editText.setText("");
                    }
                }
            });

            // Register a broadcast receiver that allows us to react in the UI when the service says
            LocalBroadcastManager.getInstance(getApplicationContext())
                    .registerReceiver(mBroadcastReceiver, new IntentFilter("UPDATE_MESSAGE_VIEW"));

            // Create adapter here directly onto the data structure of the service
            mMessageArrayAdapter = new ArrayAdapter<Message>(ChatActivity.this,
                    android.R.layout.simple_list_item_1) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    Message message = getItem(position);

                    if (convertView == null) {
                        convertView = LayoutInflater.from(getContext())
                                .inflate(R.layout.message_row, parent, false);
                    }


                    TextView chatPartnerMessageView =
                            (TextView) convertView.findViewById(R.id.messagePartnerView);
                    TextView myMessagesView =
                            (TextView) convertView.findViewById(R.id.messageMeView);
                    TextView myPartnerTimeView =
                            (TextView) convertView.findViewById(R.id.messageMeTimeView);
                    TextView chatPartnerTimeView =
                            (TextView) convertView.findViewById(R.id.messagePartnerTimeView);

                    SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm");
                    String time = formater.format(message.getTimeWritten().getTime());



                    if (message.isWrittenByMe()) {
                        myPartnerTimeView.setText(time);
                        chatPartnerTimeView.setText("");
                        myMessagesView.setText(message.getText());
                        chatPartnerMessageView.setText("");
                        if (!message.isAcked()) {
                            myMessagesView.setTextColor(Color.RED);
                        } else {
                            myMessagesView.setTextColor(Color.BLACK);
                        }
                    } else {
                        chatPartnerTimeView.setText(time);
                        myPartnerTimeView.setText("");
                        chatPartnerMessageView.setText(message.getText());
                        myMessagesView.setText("");
                    }
                    return convertView;
                }
            };

            // Get data to be displayed and add it to adapter
            TreeSet<Message> messages = mBoundService.getMessages();
            mMessageArrayAdapter.addAll(messages);

            mMessageListView = (ListView) findViewById(R.id.messageList);
            mMessageListView.setAdapter(mMessageArrayAdapter);


            Log.d(ChatActivity.class.getSimpleName(), "Chat partners name is: " +
                    mBoundService.getPartnerName());

            mBoundService.setUnreadMessages(0);
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Log.d(ChatActivity.class.getSimpleName(), "Service unbound");
            mServiceIsBound = false;
        }
    };

    @Override
    public void onDestroy() {
        mBoundService.setUnreadMessages(0);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(mBroadcastReceiver);
        Log.d(ChatActivity.class.getSimpleName(), "onDestroy() called");
        if (mServiceIsBound) {
            Log.d(ChatActivity.class.getSimpleName(), "unbinding Service");
            unbindService(mConnection);
        }
        super.onDestroy();
    }

    // This BroadcastReceiver reacts to intents the service broadcasts when it has changed a
    // message list
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mMessageArrayAdapter.clear();
            mMessageArrayAdapter.addAll(mBoundService.getMessages());
            mMessageListView.smoothScrollToPosition(mMessageArrayAdapter.getCount() - 1);
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        Intent myIntent;
        switch (menuItem.getItemId()){
            case R.id.settings :
                myIntent = new Intent(this, SettingsActivity.class);
                this.startActivity(myIntent);
                break;
            case R.id.show_partner_key :
                if(mServiceIsBound) {
                    mBoundService.shareChatPartnerInfo(this);
                } else {
                    Toast.makeText(this, "Please wait until ChatService is connected",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.add_partner_info :
                myIntent = new Intent(this, ScanKeyActivity.class);
                myIntent.putExtra("userid", mChatPartnerID);
                this.startActivity(myIntent);
                break;
            case R.id.forget_user :
                mBoundService.forgetPartner();
                finish();
                break;
            default:
                return false;
        }
        return true;
    }

}
