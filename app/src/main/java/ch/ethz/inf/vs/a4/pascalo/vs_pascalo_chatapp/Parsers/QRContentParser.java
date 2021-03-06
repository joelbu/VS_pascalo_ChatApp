package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.Parsers;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.PublicKey;
import java.util.UUID;

import ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp.ReturnTypes.ParsedQRContent;

public class QRContentParser {
    // Made with randomness from atmospheric noise provided by random.org
    private static int magicNumber = 656297891;

    public static JSONObject serialize(UUID id, String name, PublicKey key) {
        String keyStringRepresentation = KeyParser.serializePublicKey(key);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("magic", magicNumber);
            jsonObject.put("id", id.toString());
            jsonObject.put("name", name);
            jsonObject.put("key", keyStringRepresentation);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static ParsedQRContent parse(String string) {
        ParsedQRContent ret = new ParsedQRContent();

        // The magic number must be early in the string, if not don't bother trying JSON
        if (!string.substring(0, 25).contains(Integer.toString(magicNumber))) {
            ret.status = 2;
            return ret;
        }

        try {
            JSONObject json = new JSONObject(string);
            ret.id = UUID.fromString(json.getString("id"));
            ret.name = json.getString("name");
            ret.key = json.getString("key");

            ret.status = 0;
        } catch (JSONException e) {
            e.printStackTrace();
            ret.status = 1;
        }
        return ret;
    }
}
