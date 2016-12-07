package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.TreeSet;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers.MessageParser;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedMessage;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.UI.ShowKeyActivity;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers.QRContentParser;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ZXing.IntentIntegrator;

public class ChatService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ChatsHolder mChatsHolder;
    private Chat mCurrentChat;
    private boolean mChatsChanged;
    private Connector mConnector;
    private MessageParser mMessageParser;

    // flags to know if we should play sound or not resp. vibrate or not
    private boolean vibrate;
    private long vibrating_time = (long) 0.5; // in seconds
    private int vibrate_for_n_times = 2;
    private boolean sound;

    // For use in MainActivity only, ChatActivity is only supposed to interact with the current
    // chat through the methods below
    public Collection<Chat> getChats() {
        return mChatsHolder.getChats();
    }

    // Sets up the service to know what view is open in ChatActivity
    public void setChatPartner (UUID id) {
        mCurrentChat = mChatsHolder.getChat(id);
    }

    public TreeSet<Message> getMessages() {
        return mCurrentChat.getMessageList();
    }

    public String getPartnerName() {
        return mCurrentChat.getChatPartnerName();
    }

    public PublicKey getPartnerKey() {
        return mCurrentChat.getChatPartnerPublicKey();
    }

    public void setUpOwnInfo(UUID id, String name, PrivateKey privateKey, PublicKey publicKey) {
        mChatsHolder.setUpOwnInfo(id, name, privateKey, publicKey);
    }

    public void forgetPartner() {
        mChatsHolder.forget(mCurrentChat.getChatPatnerID());
        mChatsChanged = true;
    }

    // Returns 0 on success, 1 for UUID already in use
    public int addPartner(UUID id, String name, PublicKey key) {
        int status = mChatsHolder.addPartner(id, name, key);
        if (status == 0) {
            mChatsChanged = true;
        }
        return status;
    }

    public boolean getChatsChanged() {
        return mChatsChanged;
    }

    public void resetChatsChanged() {
        mChatsChanged = false;
    }

    public boolean isKeyKnown() {
        return mCurrentChat.isKeyKnown();
    }

    public void setUnreadMessages(int unreadMessages) {
        mCurrentChat.setUnreadMessages(unreadMessages);
    }

    public void shareMyInfo(Activity activity) {
        shareInfo(activity,
                mChatsHolder.getOwnId(),
                mChatsHolder.getOwnName(),
                mChatsHolder.getOwnPublicKey());
    }

    public void shareChatPartnerInfo(Activity activity) {
        shareInfo(activity,
                mCurrentChat.getChatPatnerID(),
                mCurrentChat.getChatPartnerName(),
                mCurrentChat.getChatPartnerPublicKey());
    }

    private void shareInfo(final Activity activity, UUID id, String name, PublicKey key) {
        final String info = QRContentParser.serialize(id, name, key).toString();
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.shareText(info, "TEXT_TYPE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Intent intent = new Intent(activity, ShowKeyActivity.class);
                intent.putExtra("info", info);
                activity.startActivity(intent);

            }
        });
    }

    // TODO: Better crypto? Signing a message hash and appending it? Symmetric crypto (AES?) for message content and signature, encrypting the key with RSA and appending that to the message?
    // This is going to be slow and vulnerable to malleability attacks, and we don't have any
    // sender authentication but it doesn't really matter for our project
    private byte[] encryptMessage(byte[] payload) {
        byte[] cipherText = null;
        try {
            // Ignore the warning android studio gives here about ECB,
            // it only applies to symmetric crypto
            final Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, mCurrentChat.getChatPartnerPublicKey());
            cipherText = cipher.doFinal(payload);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    private byte[] decryptMessage(byte[] cipherText){
        byte[] payload = null;
        try {
            // Ignore the warning android studio gives here about ECB,
            // it only applies to symmetric crypto
            final Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, mChatsHolder.getOwnPrivateKey());
            payload = cipher.doFinal(cipherText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return payload;
    }

    // This is intended to be called from ChatActivity after a user pressed send
    public void sendMessage(String text) {

        // The Chat has the context information necessary to construct a Message
        Message message = mCurrentChat.constructMessageFromUser(text);

        // Telling the UI that something has changed
        broadcastViewChange();

        String payload = mMessageParser.serializeForNetwork(message).toString();

        byte[] cipherText = null;
        try {
            cipherText = encryptMessage(payload.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (cipherText != null) {
            mConnector.broadcastMessage(cipherText);
        }
    }

    private void broadcastViewChange() {
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(new Intent("UPDATE_MESSAGE_VIEW"));
    }

    public void onReceiveMessage(byte[] message) {
        try {
            byte[] payload = decryptMessage(message);

            ParsedMessage ret = mMessageParser.parseFromNetwork(new String(payload, "UTF-8"));

            if (ret.status == 0) {

                mChatsHolder.addMessage(ret.sender, ret.message);

                // notification
                // TODO: add a notification in the status bar
                //create a notification
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(android.R.drawable.ic_secure)
                                .setContentTitle("Chat")
                                .setContentText("You have a new message from " + ret.sender.toString())
                                .setOngoing(true);

                //create notification manager
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                //show built notification
                notificationManager.notify(17, builder.build());

                // play sound if necessary
                if (sound) {
                    //create media player
                    AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    /*
                    MediaPlayer mp = MediaPlayer.create(
                            getApplicationContext(),
                            R.raw.loop TODO:set a file to play
                            //, new AudioAttributes.Builder().setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED).build(),
                            //am.generateAudioSessionId()
                    ); */
                    // play sound
                    //mp.start();
                }

                // vibrate if necessary
                if (vibrate) {
                    // vibrate
                    long ms = (long) (1000.0 * vibrating_time);
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    // short and esy pattern
                    for (int i=0; i<vibrate_for_n_times; i++){
                        v.vibrate(ms);
                    }
                }

                if (ret.sender.equals(mCurrentChat.getChatPatnerID())) {
                    broadcastViewChange();
                }

            }

        } catch (UnsupportedEncodingException e) {
            // This should never happen, because UTF-8 is always supported
            e.printStackTrace();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // TODO: Make sure we react to configuration changes like vibration off
        vibrate = sharedPreferences.getBoolean("check_box_vibrate", true);
        sound = sharedPreferences.getBoolean("check_box_vibrate", true);

    }

    public class LocalBinder extends Binder {
        public ChatService getService() {
            return ChatService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        mChatsHolder = new ChatsHolder();

        // Now it just loads everything upon starting, we still need lazy initialisation
        // TODO: Lazy initialisation of message threads
        mChatsHolder.readAddressBook(this);
        mChatsHolder.readAllThreads(this);

        // Since notifications must come from the service I moved this here
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        // TODO: Maybe read preferences into member fields


        // just for testing
        generateTestChats();


        mMessageParser = new MessageParser(mChatsHolder.getOwnId());

        mConnector = new Connector(this, "SuperCoolPrivateChattingApp", this);
        mConnector.connectToWDMF();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        // Sticky ensures that the Android system keeps the service around for us
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(ChatService.class.getSimpleName(), "onDestroy() called");
        mConnector.disconnectFromWDMF();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .unregisterOnSharedPreferenceChangeListener(this);

        //TODO: Writeback when something changes instead of here
        // Writing upon service shutdown may not be needed if we write whenever
        // something new happens instead, but for now it will do
        mChatsHolder.writeAddressBook(this);
        mChatsHolder.writeAllThreads(this);

    }

    // function for generating test chats
    private void generateTestChats(){
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            Log.d(this.getClass().getSimpleName(), "KeyPairGenerator generated");
            kpg.initialize(128);
            Log.d(this.getClass().getSimpleName(), "KeyPairGenerator initialized");
            KeyPair kp;
            PublicKey publicKey;

            kp =  kpg.genKeyPair();
            Log.d(this.getClass().getSimpleName(), "KeyPair for Hans generated");
            publicKey = kp.getPublic();
            Log.d(this.getClass().getSimpleName(), "publicKey copied");
            UUID uuid = UUID.randomUUID();
            mChatsHolder.addPartner(uuid, "Hans Muster", publicKey);

            mChatsHolder.addMessage(uuid, new Message(true, false, GregorianCalendar.getInstance(),
                    new VectorClock(1, 4), "Text?"));
            mChatsHolder.addMessage(uuid, new Message(true, false, GregorianCalendar.getInstance(),
                    new VectorClock(5, 5), "Text6"));
            mChatsHolder.addMessage(uuid, new Message(true, false, GregorianCalendar.getInstance(),
                    new VectorClock(7, 6), "Text8"));
            mChatsHolder.addMessage(uuid, new Message(false, true, GregorianCalendar.getInstance(),
                    new VectorClock(2, 2), "Text3"));
            mChatsHolder.addMessage(uuid, new Message(true, true, GregorianCalendar.getInstance(),
                    new VectorClock(1, 0), "Text2a"));
            mChatsHolder.addMessage(uuid, new Message(false, true, GregorianCalendar.getInstance(),
                    new VectorClock(0, 1), "Text2b"));
            mChatsHolder.addMessage(uuid, new Message(false, true, GregorianCalendar.getInstance(),
                    new VectorClock(3, 4), "Text4"));
            mChatsHolder.addMessage(uuid, new Message(false, true, GregorianCalendar.getInstance(),
                    new VectorClock(5, 6), "Text7"));
            mChatsHolder.addMessage(uuid, new Message(false, true, GregorianCalendar.getInstance(),
                    new VectorClock(0, 0), "Text1"));

            kp = kpg.genKeyPair();
            Log.d(this.getClass().getSimpleName(), "KeyPair for Max generated");
            publicKey = kp.getPublic();
            Log.d(this.getClass().getSimpleName(), "publicKey copied");
            UUID uuid1 = UUID.randomUUID();
            mChatsHolder.addPartner(uuid1, "Max Problem", publicKey);

            mChatsHolder.addMessage(uuid1, new Message(false, false, GregorianCalendar.getInstance(),
                    new VectorClock(0, 1), "test"));
            mChatsHolder.addMessage(uuid1, new Message(true, false, GregorianCalendar.getInstance(),
                    new VectorClock(1, 1), "ack"));


            UUID uuidOfAStranger = UUID.randomUUID();

            mChatsHolder.addMessage(uuidOfAStranger, new Message(false, false, GregorianCalendar.getInstance(),
                    new VectorClock(5, 5), "Hey remember me?"));
            mChatsHolder.addMessage(uuidOfAStranger, new Message(false, false, GregorianCalendar.getInstance(),
                    new VectorClock(5, 6), "We met at the bar"));
            mChatsHolder.addMessage(uuidOfAStranger, new Message(false, false, GregorianCalendar.getInstance(),
                    new VectorClock(5, 7), "Oh you don't have my key yet right.\nIt's:\nass"));
            mChatsHolder.addMessage(uuidOfAStranger, new Message(false, false, GregorianCalendar.getInstance(),
                    new VectorClock(5, 8), "MCwwDQYJKoZIhvcNAQEBBQADGwAwGAIRANxEwt5Wq8EOxq5mnz8dFCECAwEAAQ=="));
            mChatsHolder.addMessage(uuidOfAStranger, new Message(false, false, GregorianCalendar.getInstance(),
                    new VectorClock(5, 9), "So add me and write back"));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
