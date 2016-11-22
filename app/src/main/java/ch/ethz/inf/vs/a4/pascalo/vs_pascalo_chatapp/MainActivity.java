package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ChatBackgroundService mBoundService;
    private ArrayAdapter<Chat> mChatArrayAdapter;
    private boolean mServiceIsBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // register listener on chatList
        ListView chatListView = (ListView) findViewById(R.id.chatList);
        chatListView.setOnItemClickListener(this);

        // overflow menu
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);

        // First start the service to make it's lifetime independent of the activity, if it's
        // already running this triggers onStartCommand, but no second instance
        startService(new Intent(getApplicationContext(), ChatBackgroundService.class));
        // Then bind to it so we can call functions in it
        mServiceIsBound = bindService(new Intent(getApplicationContext(), ChatBackgroundService.class), mConnection,
                getApplicationContext().BIND_AUTO_CREATE);
        // The service object will become available in onServiceConnected(...) so further setup is
        // done there






        // store all chat partners (addressbook in a file) in order of most recent message
        // we need an addressbook in any case

        // store chat messages per chat in a file named "cat_[chat partner name].???"
        // a little overhead but I think it's a lot easier to store and find them even we
        // have more overhead



        // idea copy most of the code from sensor app

        // idea create a class chat that has objects with multiple fields:
            // name of the chat partner
            // number of unread messages
            // list of messages
                // messages has text and a tag which indicates if the message is mine or not (has to be displayed at the left or the right)




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
            Toast.makeText(MainActivity.this, R.string.local_service_connected,
                    Toast.LENGTH_SHORT).show();

            // Create adapter here onto the data structure of the service
            Collection<Chat> chats = mBoundService.getChats().values();
            mChatArrayAdapter = new ArrayAdapter<Chat>(MainActivity.this,
                    android.R.layout.simple_list_item_1,
                    chats.toArray(new Chat[chats.size()]));

            // chatArrayAdapter.sort( -----order function----- );
            ListView chatListView = (ListView) findViewById(R.id.chatList);
            chatListView.setAdapter(mChatArrayAdapter);

        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Toast.makeText(MainActivity.this, R.string.local_service_disconnected,
                    Toast.LENGTH_SHORT).show();
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
        switch (menuItem.getItemId()){
            case R.id.settings :
                Intent myIntent = new Intent(this, SettingsActivity.class);
                this.startActivity(myIntent);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        if (mServiceIsBound) unbindService(mConnection);
        super.onDestroy();
    }


}


