package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers;


import android.util.Base64;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyParser {


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
}
