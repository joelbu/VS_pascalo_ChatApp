package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.UI;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.preference.PreferenceManager;
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

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Chat;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ChatService;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.R;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ChatService mBoundService;
    private ArrayAdapter<Chat> mChatArrayAdapter;
    private boolean mServiceIsBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(MainActivity.class.getSimpleName(), "onCreate() called");

        // register listener on chatList
        ListView chatListView = (ListView) findViewById(R.id.chatList);
        chatListView.setOnItemClickListener(this);

        // overflow menu
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);

        // First start the service to make it's lifetime independent of the activity, if it's
        // already running this triggers onStartCommand, but no second instance
        startService(new Intent(getApplicationContext(), ChatService.class));

        Log.d(MainActivity.class.getSimpleName(), "binding Service");
        // Then bind to it so we can call functions in it
        bindService(new Intent(getApplicationContext(),
                ChatService.class), mConnection,
                Context.BIND_AUTO_CREATE);
        // The service object will become available in onServiceConnected(...) so further setup is
        // done there

    }

    protected void onRestart() {
        super.onRestart();
        Log.d(MainActivity.class.getSimpleName(), "onRestart() called");

        if (mBoundService.getChatsChanged()) {
            mChatArrayAdapter.clear();
            mChatArrayAdapter.addAll(mBoundService.getChats());
            mBoundService.resetChatsChanged();
        }

        mChatArrayAdapter.sort(Chat.COMPARATOR);

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((ChatService.LocalBinder)service).getService();
            Log.d(MainActivity.class.getSimpleName(), "Service bound");
            mServiceIsBound = true;

            Log.d(MainActivity.class.getSimpleName(), getString(R.string.local_service_connected));

            // Create adapter here onto the data structure of the service
            Collection<Chat> chats = mBoundService.getChats();
            mChatArrayAdapter = new ArrayAdapter<Chat>(MainActivity.this,
                    android.R.layout.simple_list_item_1) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    Chat chat = getItem(position);

                    if (convertView == null) {
                        convertView = LayoutInflater.from(getContext())
                                .inflate(R.layout.chat_row, parent, false);
                    }

                    TextView chatPartnerNameView =
                            (TextView) convertView.findViewById(R.id.chatListPartnerName);
                    TextView unreadMessagesView =
                            (TextView) convertView.findViewById(R.id.chatListUnread);

                    chatPartnerNameView.setText(chat.getChatPartnerName());
                    unreadMessagesView.setText(Integer.toString(chat.getUnreadMessages()));


                    return convertView;
                }
            };

            mChatArrayAdapter.addAll(mBoundService.getChats());
            mChatArrayAdapter.sort(Chat.COMPARATOR);
            ListView chatListView = (ListView) findViewById(R.id.chatList);
            chatListView.setAdapter(mChatArrayAdapter);

        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Log.d(MainActivity.class.getSimpleName(), getString(R.string.local_service_disconnected));
            mServiceIsBound = false;
            finish();
        }
    };


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        // get chat at clicked item

        // start new activity with clicked chat data
        Intent chatIntent = new Intent(this, ChatActivity.class);
        Chat selectedChat = (Chat) parent.getItemAtPosition(position);
        chatIntent.putExtra("userid", selectedChat.getChatPatnerID());
        this.startActivity(chatIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
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
            case R.id.show_key :
                if(mServiceIsBound) {
                    mBoundService.shareMyInfo(this);
                } else {
                    Toast.makeText(this, "Please wait until ChatService is connected",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.add_chat :
                myIntent = new Intent(this, ScanKeyActivity.class);
                this.startActivity(myIntent);
                break;
            case R.id.go_offline :
                Log.d(MainActivity.class.getSimpleName(), "Go Offline called");
                if (mServiceIsBound) {
                    unbindService(mConnection);
                    mServiceIsBound = false;
                }
                myIntent = new Intent(getApplicationContext(), ChatService.class);
                this.stopService(myIntent);
                finish();
                break;
            //case R.id.generate_new_login :
            //    myIntent = new Intent(getApplicationContext(), GenerateKeyActivity.class);
            //    this.startActivity(myIntent);
            //    break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        Log.d(MainActivity.class.getSimpleName(), "onDestroy() called");
        if (mServiceIsBound) {
            Log.d(MainActivity.class.getSimpleName(), "unbinding Service");
            unbindService(mConnection);
            mServiceIsBound = false;
        }
        super.onDestroy();
    }


}


