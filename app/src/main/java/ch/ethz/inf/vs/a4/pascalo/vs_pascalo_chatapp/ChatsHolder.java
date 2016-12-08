package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;


import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers.ChatParser;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers.KeyParser;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers.MessageParser;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedChatMap;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedMessageThread;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.UI.MainActivity;

public class ChatsHolder {
    final String filename = "address_book";

    private Map<UUID, Chat> mChats = new HashMap<UUID, Chat>();
    private UUID mOwnId;
    private String mOwnName;
    private PrivateKey mOwnPrivateKey;
    private PublicKey mOwnPublicKey;

    public void addMessage(UUID id, Message message) {
        if (!mChats.containsKey(id)) {
            // Someone got our key somewhere and has sent us a message or someone whom we had
            // "forgotten" is writing again. We don't know their name or key but we can make a
            // basic chat object and show their messages
            addPartner(id, "Unknown user: " + id.toString(), null);
        }
        mChats.get(id).addMessage(message);
    }

    // Either makes a new chat or updates an old one
    // Returns 0 if there is a new one
    public int addPartner(UUID id, String username, PublicKey publicKey) {
        if (mChats.containsKey(id)) {
            Chat chat = mChats.get(id);
            chat.setChatPartnerName(username);
            chat.setChatPartnerPublicKey(publicKey);
            return 1;
        } else {
            Chat chat = new Chat(id, username, publicKey);
            mChats.put(id, chat);
            return 0;
        }
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

    public UUID getOwnId() {
        return mOwnId;
    }

    public String getOwnName() {
        return mOwnName;
    }

    public PrivateKey getOwnPrivateKey() {
        return mOwnPrivateKey;
    }

    public PublicKey getOwnPublicKey() {
        return mOwnPublicKey;
    }

    public void setUpOwnInfo(UUID id, String name, PrivateKey privateKey, PublicKey publicKey, Context context) {
        mOwnId = id;
        mOwnName = name;
        mOwnPrivateKey = privateKey;
        mOwnPublicKey = publicKey;
        // Our identity is so important that we write it to storage immediately
        writeAddressBook(context);
    }

    // Writing to filesystem

    public void writeAddressBook(Context context) {
        JSONObject addressBook = ChatParser.serializeCollectionOfChats(mChats.values());
        try {
            addressBook.put("ownId", mOwnId.toString());
            addressBook.put("ownName", mOwnName);
            addressBook.put("ownPrivateKey", KeyParser.serializePrivateKey(mOwnPrivateKey));
            addressBook.put("ownPublicKey", KeyParser.serializePublicKey(mOwnPublicKey));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(addressBook.toString().getBytes("UTF-8"));
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

            JSONObject addressBook = new JSONObject(new String(data, "UTF-8"));

            mOwnId = UUID.fromString(addressBook.getString("ownId"));
            mOwnName = addressBook.getString("ownName");
            mOwnPrivateKey = KeyParser.parsePrivateKey(addressBook.getString("ownPrivateKey"));
            mOwnPublicKey = KeyParser.parsePublicKey(addressBook.getString("ownPublicKey"));

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
