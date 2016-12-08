package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.TreeSet;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers.KeyParser;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers.MessageParser;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedAesKey;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedIvKeyPayload;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedMessage;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.UI.ShowKeyActivity;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers.QRContentParser;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ZXing.IntentIntegrator;

public class ChatService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public static final int AES_KEY_LENGTH = 192;
    public static final int RSA_KEY_LENGTH = 1024;
    private static final long VIBRATION_TIME = 500L; //in milliseconds
    private static final int TIMES_OF_VIBRATION = 2;

    private ChatsHolder mChatsHolder;
    private Chat mCurrentChat;
    private boolean mChatsChanged;
    private Connector mConnector;
    private MessageParser mMessageParser;

    // flags to know if we should play mSound or not resp. mVibrate or not
    private boolean mVibrate;
    private boolean mSound;

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
        mChatsHolder.setUpOwnInfo(id, name, privateKey, publicKey, this);
        mMessageParser = new MessageParser(mChatsHolder.getOwnId());
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

    // TODO: Signing a message hash and appending it?
    // This is going to be slow and vulnerable to malleability attacks, and we don't have any
    // sender authentication but it doesn't really matter for our project
    private byte[] encryptMessage(byte[] payload) {
        try {

            // Generate a random initialisation vector for each message
            SecureRandom random = new SecureRandom();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(random.generateSeed(16));
            byte[] ivRawData = ivParameterSpec.getIV();

            // Generate a random AES key for each message
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(AES_KEY_LENGTH);
            SecretKey aesKey = keyGenerator.generateKey();

            // Ignore the warning android studio gives here about ECB,
            // it only applies to symmetric crypto
            final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            rsaCipher.init(Cipher.ENCRYPT_MODE, mCurrentChat.getChatPartnerPublicKey());
            byte[] serialized = KeyParser.serializeAesKey(aesKey);
            Log.d(this.getClass().getSimpleName(), "Length of AES key and magic in Base64 is: " + serialized.length + "Bytes");
            byte[] encryptedAesKey = rsaCipher.doFinal(serialized);

            // Encrypt the payload
            final Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, ivParameterSpec);
            byte[] encryptedPayload = aesCipher.doFinal(payload);

            // Put the three iv, key and payload together. Note iv doesn't need to be encrypted.
            return KeyParser.serializeIvKeyPayload(ivRawData, encryptedAesKey, encryptedPayload);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                BadPaddingException | IllegalBlockSizeException |
                InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return new byte[0];
        }

    }

    private byte[] decryptMessage(byte[] cipherText){
        try {
            // Parse the wire format into the three byte arrays
            ParsedIvKeyPayload ret = KeyParser.parseIvKeyPayload(cipherText);

            if (ret.status != 0) {
                Log.d(this.getClass().getSimpleName(), "The message being decrypted is broken");
                return new byte[0];
            }

            // Ignore the warning android studio gives here about ECB,
            // it only applies to symmetric crypto
            final Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, mChatsHolder.getOwnPrivateKey());

            // Decrypt the AES key with our RSA private key
            byte[] aesKeyRaw = cipher.doFinal(ret.key);

            // Parse it and check for magic value
            ParsedAesKey aesRet = KeyParser.parseAesKey(aesKeyRaw);

            if (aesRet.status == 1) {
                Log.d(this.getClass().getSimpleName(), "The message being decrypted is broken");
                return new byte[0];
            } else if (aesRet.status == 2) {
                Log.d(this.getClass().getSimpleName(), "The message being decrypted is not for us");
                return new byte[0];
            }

            // Recreate initialisation vector
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ret.iv);

            // Instantiate AES Cipher with key and iv
            final Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aesCipher.init(Cipher.DECRYPT_MODE, aesRet.key, ivParameterSpec);

            // Decrypt payload with AES
            return aesCipher.doFinal(ret.payload);


        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                BadPaddingException | IllegalBlockSizeException |
                InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return new byte[0];
        }
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

                // play mSound if necessary
                if (mSound) {
                    //create media player
                    AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    /*
                    MediaPlayer mp = MediaPlayer.create(
                            getApplicationContext(),
                            R.raw.loop TODO:set a file to play
                            //, new AudioAttributes.Builder().setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED).build(),
                            //am.generateAudioSessionId()
                    ); */
                    // play mSound
                    //mp.start();
                }

                // mVibrate if necessary
                if (mVibrate) {
                    // mVibrate

                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    // short and esy pattern
                    for (int i = 0; i < TIMES_OF_VIBRATION; i++){
                        v.vibrate(VIBRATION_TIME);
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
        mVibrate = sharedPreferences.getBoolean("check_box_vibrate", true);
        mSound = sharedPreferences.getBoolean("check_box_vibrate", true);

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
            kpg.initialize(RSA_KEY_LENGTH);
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
                    new VectorClock(5, 8), "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDbW7VLPFD0bsagHIC0vnSFbHn7RsbXeniDqwN-6j63HYLoGpvWcWJL8hQgVAHVwpEkqQYwQdzmh0RY55sikG-9R-X3KDd5mGghumMIlc7dPau1JlxWqMjSHJ3AmggHkJr0c4QeL2_u-9GqDpTmjTI77-mntU1mrD-XU-gPOx8QSwIDAQAB"));
            mChatsHolder.addMessage(uuidOfAStranger, new Message(false, false, GregorianCalendar.getInstance(),
                    new VectorClock(5, 9), "So add me and write back"));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
