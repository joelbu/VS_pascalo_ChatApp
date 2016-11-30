package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.UI;

import android.content.BroadcastReceiver;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ChatService;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Message;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.R;

public class ChatActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private ChatService mBoundService;
    private ArrayAdapter<Message> mMessageArrayAdapter;
    private UUID mChatPartnerID;
    private boolean mServiceIsBound;

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

        mServiceIsBound = bindService(new Intent(getApplicationContext(),
                ChatService.class), mConnection,
                getApplicationContext().BIND_AUTO_CREATE);

        // register listener on chatList
        ListView chatListView = (ListView) findViewById(R.id.messageList);
        chatListView.setOnItemClickListener(this);
            // why we need a listener on chatListView???

        // read information out of intent

        // name of the chat partner

        // filename of chatfile

        // load chat messages out of file

        // Register a broadcast receiver that allows us to react in the UI when the service says
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(mBroadcastReceiver, new IntentFilter("update-message-list"));


    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((ChatService.LocalBinder)service).getService();

            // Tell the user about this for our demo.
            Toast.makeText(ChatActivity.this, R.string.local_service_connected,
                    Toast.LENGTH_SHORT).show();

            // Tell the service who the current partner is, so it knows on which chats to
            // call the functions
            mBoundService.setChatPartner(mChatPartnerID);

            // create adapter
            Collection<Message> messages =
                    (Collection<Message>) mBoundService.getMessages();

            // Create adapter here directly onto the data structure of the service
            mMessageArrayAdapter = new ArrayAdapter<Message>(ChatActivity.this,
                    android.R.layout.simple_list_item_1,
                    messages.toArray(new Message[messages.size()])) {
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

                    if (message.isWrittenByMe()) {
                        myMessagesView.setText(message.getText());
                        chatPartnerMessageView.setText("");
                        if (!message.isAcked()) {
                            myMessagesView.setTextColor(Color.RED);
                        } else {
                            myMessagesView.setTextColor(Color.BLACK);
                        }
                    } else {
                        chatPartnerMessageView.setText(message.getText());
                        myMessagesView.setText("");
                        message.setAcked(true);
                    }
                    return convertView;
                }
            };

            mMessageArrayAdapter.sort(new Comparator<Message>() {
            @Override
            public int compare(Message message1, Message message2) {
                if (message1.getClock().happenedBefore(message2.getClock())) {
                    return -1;
                } else if (message2.getClock().happenedBefore(message1.getClock())) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
            ListView messageListView = (ListView) findViewById(R.id.messageList);
            messageListView.setAdapter(mMessageArrayAdapter);

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
            Toast.makeText(ChatActivity.this, R.string.local_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onDestroy() {
        // save unsent messages with Tag "unsent" in chatfile
        LocalBroadcastManager.getInstance(getApplicationContext())
                                .unregisterReceiver(mBroadcastReceiver);
        if (mServiceIsBound) unbindService(mConnection);
        super.onDestroy();
    }

    // This BroadcastReceiver reacts to intents the service broadcasts when it has changed a
    // message list
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mMessageArrayAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

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
                myIntent = new Intent(this, ShowKeyActivity.class);
                this.startActivity(myIntent);
                break;
            case R.id.forget_user :
                break;
            default:
                return false;
        }
        return true;
    }

}
