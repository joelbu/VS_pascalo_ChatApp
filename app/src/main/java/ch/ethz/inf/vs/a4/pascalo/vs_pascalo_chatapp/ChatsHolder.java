package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;


import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers.ChatParser;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedChatMap;

public class ChatsHolder {
    final String filename = "address_book";

    private Map<UUID, Chat> mChats = new HashMap<UUID, Chat>();

    public void addMessage(UUID uuid, Message message) {
        mChats.get(uuid).addMessage(message);
        mChats.get(uuid).updateRecentActivity();
        if (!message.isAcked()) {
            mChats.get(uuid).setUnreadMessages(mChats.get(uuid).getUnreadMessages() + 1);
        }
    }

    // Returns 0 on success, 1 for UUID already in use
    public int addPartner(UUID id, String username, String publicKey) {
        if (mChats.containsKey(id)) return 1;

        Chat chat = new Chat(id, username, publicKey, new LinkedList<Message>());
        mChats.put(id, chat);
        return 0;
    }

    public Collection<Chat> getChats() {
        return mChats.values();
    }

    public Chat getChat(UUID id) {
        return mChats.get(id);
    }

    public LinkedList<Message> getPartnerMessages(UUID id) {
        return mChats.get(id).getMessageList();
    }

    public String getPartnerName(UUID id) {
        return mChats.get(id).getChatPartnerName();
    }

    public void writeAddressBook(Context context) {
        FileOutputStream outputStream;

        ChatParser parser = new ChatParser();
        String addressBook = parser.serializeCollectionOfChats(mChats.values()).toString();

        try {
            outputStream = context.openFileOutput(filename, context.getApplicationContext().MODE_PRIVATE);
            outputStream.write(addressBook.getBytes("UTF-8"));
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readAddressBook(Context context) {
        FileInputStream inputStream;

        ChatParser parser = new ChatParser();

        try {
            inputStream = context.openFileInput(filename);
            String address_bookIn = inputStream.toString();
            inputStream.close();

            ParsedChatMap ret = parser.parseMapOfChats(address_bookIn);
            if (ret.status == 0) {
                mChats = ret.chat;
            } else {
                Log.d(ChatsHolder.class.getName(), "Failed to load address book.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
