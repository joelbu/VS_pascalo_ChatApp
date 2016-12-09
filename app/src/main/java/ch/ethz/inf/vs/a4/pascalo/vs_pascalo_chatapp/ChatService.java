package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
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
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.UI.MainActivity;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.UI.ShowKeyActivity;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers.QRContentParser;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ZXing.IntentIntegrator;

public class ChatService extends Service {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public static final int AES_KEY_LENGTH = 192;
    public static final int RSA_KEY_LENGTH = 1024;
    private static final long VIBRATION_TIME = 300L; //in milliseconds
    private static final long VIBRATION_SLEEP = 500L; //time to sleep
    private static final int TIMES_OF_VIBRATION = 1;
    private static final long [] VIBRATION_PATTERN = {0, VIBRATION_TIME, VIBRATION_SLEEP, VIBRATION_TIME, VIBRATION_SLEEP}; // generate a vibrationPattern


    private ChatsHolder mChatsHolder;
    private UUID mCurrentChatId;
    private boolean mChatsChanged;
    private Connector mConnector;
    private MessageParser mMessageParser;

    // flags to know if we should play mSound or not resp. mVibrate or not
    private boolean mVibrate;
    private boolean mSound;
    private boolean mInAppVibration;


    // For use in MainActivity only, ChatActivity is only supposed to interact with the current
    // chat through the methods below
    public Collection<Chat> getChats() {
        return mChatsHolder.getChats();
    }

    // Sets up the service to know what view is open in ChatActivity
    public void setChatPartner (UUID id) {
        mCurrentChatId = id;
    }

    public TreeSet<Message> getMessages() {
        return mChatsHolder.getMessages(mCurrentChatId);
    }

    public String getPartnerName() {
        return mChatsHolder.getPartnerName(mCurrentChatId);
    }

    public PublicKey getPartnerKey() {
        return mChatsHolder.getPartnerKey(mCurrentChatId);
    }

    public void setUpOwnInfo(UUID id, String name, PrivateKey privateKey, PublicKey publicKey) {
        mChatsHolder.setUpOwnInfo(id, name, privateKey, publicKey);
        mMessageParser = new MessageParser(mChatsHolder.getOwnId());
    }

    public void forgetPartner() {
        mChatsHolder.forget(mCurrentChatId);
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
        return mChatsHolder.isKeyKnown(mCurrentChatId);
    }

    public void setUnreadMessages(int unreadMessages) {
        mChatsHolder.setUnreadMessages(mCurrentChatId, unreadMessages);
    }

    public void shareMyInfo(Activity activity) {
        shareInfo(activity,
                mChatsHolder.getOwnId(),
                mChatsHolder.getOwnName(),
                mChatsHolder.getOwnPublicKey());
    }

    public void shareChatPartnerInfo(Activity activity) {
        shareInfo(activity,
                mCurrentChatId,
                mChatsHolder.getPartnerName(mCurrentChatId),
                mChatsHolder.getPartnerKey(mCurrentChatId));
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

    public void setCurrentChatOpenInfo(boolean b) {
        mChatsHolder.setOpenInActivity(mCurrentChatId, b);
    }

    public boolean getCurrentChatOpenInfo() {
        return mChatsHolder.isOpenInActivity(mCurrentChatId);
    }

    // TODO: Signing a message hash and appending it?
    // This is going to be slow and vulnerable to malleability attacks, and we don't have any
    // sender authentication but it doesn't really matter for our project
    private byte[] encryptMessage(byte[] payload, UUID receiver) {
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
            rsaCipher.init(Cipher.ENCRYPT_MODE, mChatsHolder.getPartnerKey(receiver));
            byte[] serialized = KeyParser.serializeAesKey(aesKey);
            Log.d(this.getClass().getSimpleName(), "Length of AES key and magic in Base64 is: " +
                    serialized.length + "Bytes");
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

    private void prepareAndSendAcknowledgement(UUID receiver, Message message) {
        Message ack = new Message(
                true,
                true,
                true,
                null,
                new VectorClock(message.getClock()),
                null);

        sendMessage(receiver, ack);
    }

    // This is intended to be called from ChatActivity after a user pressed send
    public void prepareAndSendMessage(String text) {
        // The Chat has the context information necessary to construct a Message
        Message message = mChatsHolder.constructMessageFromUser(mCurrentChatId, text);

        // Telling the UI that something has changed
        broadcastViewChange();

        mChatsChanged = true;

        sendMessage(mCurrentChatId, message);
    }


    private void sendMessage(UUID receiver, Message message) {
        String payload = mMessageParser.serializeForNetwork(message).toString();

        byte[] cipherText = null;
        try {
            cipherText = encryptMessage(payload.getBytes("UTF-8"), receiver);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (cipherText != null) {

            if (receiver.equals(mChatsHolder.getOwnId())) {

                Log.d(this.getClass().getSimpleName(), "Writing to ourselves");
                Log.d(this.getClass().getSimpleName(), "Is this an ack?: " + String.valueOf(message.isAckMessage()));
                Log.d(this.getClass().getSimpleName(), "This has a VC of: " + String.valueOf(message.getClock().serializeForNetwork()));
                final byte[] cipherTextDelayed = cipherText;

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        onReceiveMessage(cipherTextDelayed);
                    }

                }, 5000);


            } else {
                mConnector.broadcastMessage(cipherText);
            }


        }
    }

    private void broadcastViewChange() {
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(new Intent("UPDATE_MESSAGE_VIEW"));
    }

    public void onReceiveMessage(byte[] message) {
        Log.d(ChatService.this.getClass().getSimpleName(), "onReceiveMessage is called");
        try {
            byte[] payload = decryptMessage(message);

            ParsedMessage ret = mMessageParser.parseFromNetwork(new String(payload, "UTF-8"));

            if (ret.status == 0) { // We just got a standard message

                mChatsHolder.addMessage(ret.sender, ret.message);
                mChatsChanged = true;
                prepareAndSendAcknowledgement(ret.sender, ret.message);

                // Only make notification if the chat is not currently open
                    // if the last active chat was the mCurrentChat there will be no notification

                if(!(ret.sender.equals(mCurrentChatId)
                        && getCurrentChatOpenInfo())) { // Chat is not open

                    // notification
                    // TODO: modifiy notation content
                    //create a notification
                    Log.d(ChatService.this.getClass().getSimpleName(), "Before creating notification");
                    NotificationCompat.Builder builder =
                            new NotificationCompat.Builder(this)
                                    .setSmallIcon(android.R.drawable.ic_secure)
                                    .setContentTitle("SecureChat")
                                    .setContentText("You have a new message from "
                                            + mChatsHolder.getPartnerName(mCurrentChatId))
                                    .setOngoing(false)
                                    .setAutoCancel(true)
                                    .setContentIntent(PendingIntent.getActivity(
                                            this,
                                            (int) System.currentTimeMillis(),
                                            new Intent(this, MainActivity.class),
                                            0)
                                    );


                    //create notification manager
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    Log.d(ChatService.this.getClass().getSimpleName(), "before notifying");

                    //show built notification
                    notificationManager.notify(17, builder.build());

                    // play mSound if necessary
                    if (mSound) {

                        Log.d(ChatService.this.getClass().getSimpleName(), "playing a sound");

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

                        Log.d(ChatService.this.getClass().getSimpleName(), "vibrate");

                        // mVibrate

                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        // short and esy pattern
                        v.vibrate(VIBRATION_PATTERN, -1);
                    }

                } else  { // Chat is indeed currently open
                    broadcastViewChange();

                    if (mInAppVibration) {
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        // vibrate for 200ms
                        v.vibrate(200L);
                    }
                }

            } else if (ret.status == 1) { // We just got an ack message
                mChatsHolder.markMessageAcknowledged(ret.sender, ret.message);
                if (ret.sender.equals(mCurrentChatId)) {
                    broadcastViewChange();
                }
            }

        } catch (UnsupportedEncodingException e) {
            // This should never happen, because UTF-8 is always supported
            e.printStackTrace();
        }
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mOnSPChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sP, String s) {
                    mVibrate = sP.getBoolean("check_box_vibrate", true);
                    mSound = sP.getBoolean("check_box_vibrate", true);
                    mInAppVibration = sP.getBoolean("check_box_inAppVibration", true);
                }
            };



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
        super.onCreate();
        mChatsHolder = new ChatsHolder(getApplicationContext());

        // Now it just loads everything upon starting, we still need lazy initialisation
        // TODO: Lazy initialisation of message threads

        mChatsHolder.readAddressBook();
        mChatsHolder.readAllThreads();

        // Since notifications must come from the service I moved this here
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        sharedPreferences.registerOnSharedPreferenceChangeListener(mOnSPChangeListener);

        mVibrate = sharedPreferences.getBoolean("check_box_vibrate", true);
        mSound = sharedPreferences.getBoolean("check_box_vibrate", true);
        mInAppVibration = sharedPreferences.getBoolean("check_box_inAppVibration", true);


        mMessageParser = new MessageParser(mChatsHolder.getOwnId());

        mConnector = new Connector(this, "SuperCoolPrivateChattingApp", this);
        mConnector.connectToWDMF();

        //testingNotification();

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
                .unregisterOnSharedPreferenceChangeListener(mOnSPChangeListener);
    }

    // function for generating test chats
    public void generateTestChats(){
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

            mChatsHolder.addMessage(uuid, new Message(true, false, false,
                    GregorianCalendar.getInstance(), new VectorClock(1, 4), "Text?"));
            mChatsHolder.addMessage(uuid, new Message(true, false, false,
                    GregorianCalendar.getInstance(), new VectorClock(5, 5), "Text6"));
            mChatsHolder.addMessage(uuid, new Message(true, false, false,
                    GregorianCalendar.getInstance(), new VectorClock(7, 6), "Text8"));
            mChatsHolder.addMessage(uuid, new Message(false, true, false,
                    GregorianCalendar.getInstance(), new VectorClock(2, 2), "Text3"));
            mChatsHolder.addMessage(uuid, new Message(true, true, false,
                    GregorianCalendar.getInstance(), new VectorClock(1, 0), "Text2a"));
            mChatsHolder.addMessage(uuid, new Message(false, true, false,
                    GregorianCalendar.getInstance(), new VectorClock(0, 1), "Text2b"));
            mChatsHolder.addMessage(uuid, new Message(false, true, false,
                    GregorianCalendar.getInstance(), new VectorClock(3, 4), "Text4"));
            mChatsHolder.addMessage(uuid, new Message(false, true, false,
                    GregorianCalendar.getInstance(), new VectorClock(5, 6), "Text7"));
            mChatsHolder.addMessage(uuid, new Message(false, true, false,
                    GregorianCalendar.getInstance(),  new VectorClock(0, 0), "Text1"));

            kp = kpg.genKeyPair();
            Log.d(this.getClass().getSimpleName(), "KeyPair for Max generated");
            publicKey = kp.getPublic();
            Log.d(this.getClass().getSimpleName(), "publicKey copied");
            UUID uuid1 = UUID.randomUUID();
            mChatsHolder.addPartner(uuid1, "Max Problem", publicKey);

            mChatsHolder.addMessage(uuid1, new Message(false, false, false,
                    GregorianCalendar.getInstance(), new VectorClock(0, 1), "test"));
            mChatsHolder.addMessage(uuid1, new Message(true, false, false,
                    GregorianCalendar.getInstance(), new VectorClock(1, 1), "ack"));


            UUID uuidOfAStranger = UUID.randomUUID();

            mChatsHolder.addMessage(uuidOfAStranger, new Message(false, false, false,
                    GregorianCalendar.getInstance(), new VectorClock(5, 5), "Hey remember me?"));
            mChatsHolder.addMessage(uuidOfAStranger, new Message(false, false, false,
                    GregorianCalendar.getInstance(), new VectorClock(5, 6), "We met at the bar"));
            mChatsHolder.addMessage(uuidOfAStranger, new Message(false, false, false,
                    GregorianCalendar.getInstance(), new VectorClock(5, 7), "Oh you don't have my key yet right.\nIt's:\nass"));
            mChatsHolder.addMessage(uuidOfAStranger, new Message(false, false, false,
                    GregorianCalendar.getInstance(), new VectorClock(5, 8), "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDbW7VLPFD0bsagHIC0vnSFbHn7RsbXeniDqwN-6j63HYLoGpvWcWJL8hQgVAHVwpEkqQYwQdzmh0RY55sikG-9R-X3KDd5mGghumMIlc7dPau1JlxWqMjSHJ3AmggHkJr0c4QeL2_u-9GqDpTmjTI77-mntU1mrD-XU-gPOx8QSwIDAQAB"));
            mChatsHolder.addMessage(uuidOfAStranger, new Message(false, false, false,
                    GregorianCalendar.getInstance(), new VectorClock(5, 9), "So add me and write back"));

            mChatsHolder.addPartner(mChatsHolder.getOwnId(), "Myself", mChatsHolder.getOwnPublicKey());

            mChatsHolder.addMessage(mChatsHolder.getOwnId(), new Message(true, true, false,
                    GregorianCalendar.getInstance(), new VectorClock(0, 1), "Okay let's see"));

            mChatsChanged = true;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void testingNotification() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // short and esy pattern
        v.vibrate(VIBRATION_PATTERN, -1);
    }
}
