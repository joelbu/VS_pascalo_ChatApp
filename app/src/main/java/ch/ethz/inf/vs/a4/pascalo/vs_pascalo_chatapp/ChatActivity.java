package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private ChatBackgroundService mBoundService;
    private ArrayAdapter<Message> mMessageArrayAdapter;
    private UUID mChatPartnerID;
    private boolean mServiceIsBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        mChatPartnerID = (UUID) intent.getSerializableExtra("userid");

        Toast.makeText(ChatActivity.this, "Chat partner is: " + mChatPartnerID.toString(),
                Toast.LENGTH_SHORT).show();

        mServiceIsBound = bindService(new Intent(getApplicationContext(),
                ChatBackgroundService.class), mConnection,
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




        ////////////////////////////////////////////////////////////////////////////////////////////
        // JUST FOR TESTING


        // create adapter
        Collection<Message> messages =
                (Collection<Message>) mBoundService.getChats().get(mChatPartnerID).getMessageList().clone();


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

                if (mMessageArrayAdapter.getItem(position).getWrittenByMe()) {
                    chatPartnerMessageView.setText(mMessageArrayAdapter.getItem(position).toString());
                } else {
                    myMessagesView.setText(mMessageArrayAdapter.getItem(position).toString());
                }
                return convertView;
            }
        };



    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((ChatBackgroundService.LocalBinder)service).getService();

            // Tell the user about this for our demo.
            Toast.makeText(ChatActivity.this, R.string.local_service_connected,
                    Toast.LENGTH_SHORT).show();

            // Create adapter here directly onto the data structure of the service
            mMessageArrayAdapter = new ArrayAdapter<Message>(ChatActivity.this,
                    android.R.layout.simple_list_item_1,
                    mBoundService.getChats().get(mChatPartnerID).getMessageList());

            // chatArrayAdapter.sort( -----order function----- );
            ListView messageListView = (ListView) findViewById(R.id.messageList);
            messageListView.setAdapter(mMessageArrayAdapter);

            Toast.makeText(ChatActivity.this, "Chat partners name is: " + mBoundService.getChats()
                    .get(mChatPartnerID).getChatPartnerName(), Toast.LENGTH_SHORT).show();

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
}
