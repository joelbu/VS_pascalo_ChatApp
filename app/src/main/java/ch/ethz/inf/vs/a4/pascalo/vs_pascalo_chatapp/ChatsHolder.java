package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;


import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers.ChatParser;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers.MessageParser;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedChatMap;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedMessageThread;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.UI.MainActivity;

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

    public void forget(UUID id) {
        Log.d(ChatsHolder.class.getSimpleName(), "forgetting user: "+ id.toString());
        mChats.remove(id);
    }

    // Writing to filesystem

    public void writeAddressBook(Context context) {
        String addressBook = ChatParser.serializeCollectionOfChats(mChats.values()).toString();
        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(addressBook.getBytes("UTF-8"));
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeAllThreads(Context context) {
        for (UUID uuid : mChats.keySet()) {
            writeThread(context, uuid);
        }
    }

    public void writeThread(Context context, UUID uuid) {
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput("chat_with_" + uuid.toString(),
                    Context.MODE_PRIVATE);
            outputStream.write(
                    MessageParser.serializeThreadForStorage(mChats.get(uuid).getMessageList())
                            .toString().getBytes("UTF-8"));
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






    // Reading from filesystem

    public void readAddressBook(Context context) {
        try {
            File internalAppDir = context.getFilesDir();
            File addressBookFile = new File(internalAppDir, filename);
            FileInputStream fileInputStream =  new FileInputStream(addressBookFile);

            byte[] data = new byte[(int) addressBookFile.length()];
            fileInputStream.read(data);
            fileInputStream.close();

            String addressBook = new String(data, "UTF-8");

            ParsedChatMap parsedChatMap = ChatParser.parseMapOfChats(addressBook);
            if (parsedChatMap.status == 0) {
                mChats = parsedChatMap.chat;
            } else {
                Log.d(ChatsHolder.class.getSimpleName(), "Failed to load address book.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void readAllThreads(Context context) {
        for (UUID uuid : mChats.keySet()) {
            readThread(context, uuid);
        }
    }

    public void readThread(Context context, UUID uuid) {
        FileInputStream inputStream;
        try {
            File internalAppDir = context.getFilesDir();
            File threadFile = new File(internalAppDir, "chat_with_" + uuid.toString());
            FileInputStream fileInputStream =  new FileInputStream(threadFile);

            byte[] data = new byte[(int) threadFile.length()];
            fileInputStream.read(data);
            fileInputStream.close();

            String thread = new String(data, "UTF-8");

            ParsedMessageThread parsedMessageThread =
                    MessageParser.parseThreadFromStorage(thread);
            if (parsedMessageThread.status == 0) {
                mChats.get(uuid).setMessageList(parsedMessageThread.messages);
            } else {
                Log.d(ChatsHolder.class.getSimpleName(), "Failed to load thread.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
