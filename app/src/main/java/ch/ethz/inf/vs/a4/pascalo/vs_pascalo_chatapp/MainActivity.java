package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {


    private LinkedList<Chat> chatList = new LinkedList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // register listener on chatList
        ListView chatListView = (ListView) findViewById(R.id.chatList);
        chatListView.setOnItemClickListener(this);

        // overflow menu
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);


        // TODO: set chats to list
        Chat temp = new Chat("Hans Muster", "", new LinkedList<Message>());
        chatList.add(temp);
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

        ArrayAdapter<Chat> chatArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                chatList);

        // chatArrayAdapter.sort( -----order function----- );

        chatListView.setAdapter(chatArrayAdapter);


    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        // get chat at clicked item

        // start new activity with clicked chat data
        Intent chatIntent = new Intent(this, ChatActivity.class);
        this.startActivity(chatIntent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

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
        }
        return true;
    }

}
