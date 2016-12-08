package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers;


import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedAesKey;
import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedIvKeyPayload;

public class KeyParser {
    // Made with randomness from atmospheric noise provided by random.org
    private static int magicNumber = -432185306;
    private static String TAG = "KeyParser";

    public static String serializePrivateKey(PrivateKey key) {
        String keyStringRepresentation = "";
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = factory.getKeySpec(key, PKCS8EncodedKeySpec.class);
            keyStringRepresentation = Base64.encodeToString(keySpec.getEncoded(), Base64.NO_WRAP);

        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return keyStringRepresentation;
    }

    public static PrivateKey parsePrivateKey(String string) {
        try {
            byte[] keyByte = Base64.decode(string, Base64.NO_WRAP);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyByte);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }



    public static String serializePublicKey(PublicKey key) {
        String keyStringRepresentation = "";
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            Log.d(TAG, "The KeyFactory is: " + factory.toString());
            if (key == null) {
                Log.d(TAG, "We should not get to this point.");
                return keyStringRepresentation;
            }
            Log.d(TAG, "The Key is: " + key.toString());
            Log.d(TAG, "The EncodedKeySpec is: " + X509EncodedKeySpec.class.toString());
            X509EncodedKeySpec keySpec = factory.getKeySpec(key, X509EncodedKeySpec.class);
            keyStringRepresentation = Base64.encodeToString(keySpec.getEncoded(), Base64.NO_WRAP);

        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return keyStringRepresentation;
    }

    public static PublicKey parsePublicKey(String string) {
        try {
            byte[] keyByte = Base64.decode(string, Base64.NO_WRAP);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyByte);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }



    public static byte[] serializeAesKey(SecretKey key) {
        try {
            JSONObject json = new JSONObject();
            json.put("m", magicNumber);
            json.put("k", Base64.encodeToString(key.getEncoded(), Base64.NO_WRAP));
            return json.toString().getBytes("UTF-8");

        } catch (JSONException |UnsupportedEncodingException e) {

            e.printStackTrace();
            return new byte[0];
        }
    }

    public static ParsedAesKey parseAesKey(byte[] bytes) {
        ParsedAesKey ret = new ParsedAesKey();
        try {
            String string = new String(bytes, "UTF-8");
            JSONObject json;

            // The magic number must be early in the string, if not don't bother trying JSON
            if (!string.substring(0, 15).contains(Integer.toString(magicNumber)))  {
                ret.status = 2;
                return ret;
            }

            json = new JSONObject(string);
            byte[] keyRaw = Base64.decode(json.getString("k"), Base64.NO_WRAP);
            ret.key = new SecretKeySpec(keyRaw, "AES");
            ret.status = 0;

        } catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
            ret.status = 1;
        }

        return ret;
    }


    public static byte[] serializeIvKeyPayload(byte[] iv, byte[] key, byte[] payload) {
        JSONObject json = new JSONObject();
        try {
            json.put("iv", Base64.encodeToString(iv, Base64.NO_WRAP));
            json.put("key", Base64.encodeToString(key, Base64.NO_WRAP));
            json.put("payload", Base64.encodeToString(payload, Base64.NO_WRAP));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            return json.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static ParsedIvKeyPayload parseIvKeyPayload(byte[] bundle) {
        ParsedIvKeyPayload ret = new ParsedIvKeyPayload();
        try {
            String jsonString = new String(bundle, "UTF-8");
            JSONObject json = new JSONObject(jsonString);

            ret.iv = Base64.decode(json.getString("iv"), Base64.NO_WRAP);
            ret.key = Base64.decode(json.getString("key"), Base64.NO_WRAP);
            ret.payload = Base64.decode(json.getString("payload"), Base64.NO_WRAP);
            ret.status = 0;
        } catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
            ret.status = 1;
        }

        return ret;
    }
}
