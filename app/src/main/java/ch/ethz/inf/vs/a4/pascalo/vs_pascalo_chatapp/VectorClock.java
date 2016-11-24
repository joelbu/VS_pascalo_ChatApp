package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import org.json.JSONException;
import org.json.JSONObject;

public class VectorClock  {
    private int myTime;
    private int theirTime;

    public VectorClock() {
    }

    public VectorClock(int myTime, int theirTime) {
        this.myTime = myTime;
        this.theirTime = theirTime;
    }

    public void takeMax(VectorClock other) {
        if (other.myTime > myTime) { myTime = other.myTime; }
        if (other.theirTime > theirTime) { theirTime = other.theirTime; }
    }

    public void tick() {
        myTime = myTime + 1;
    }

    public boolean happenedBefore(VectorClock other) {
        boolean atLeastOneLater = false;
        boolean atLeastOneEarlier = false;

        atLeastOneEarlier = myTime < other.myTime || theirTime < other.theirTime;
        atLeastOneLater = myTime > other.myTime || theirTime > other.theirTime;

        return atLeastOneEarlier && !atLeastOneLater;
    }

    public String serializeForStorage() {
        JSONObject json = new JSONObject();
        try {
            json.put("myTime", myTime);
            json.put("theirTime", theirTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    public void parseFromStorage(String string) {
        try {
            JSONObject json = new JSONObject(string);
            myTime = json.getInt("myTime");
            theirTime = json.getInt("theirTime");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String serializeForNetwork() {
        JSONObject json = new JSONObject();
        try {
            json.put("senderTime", myTime);
            json.put("receiverTime", theirTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    public void parseFromNetwork(String string) {
        try {
            JSONObject json = new JSONObject(string);
            myTime = json.getInt("receiverTime");
            theirTime = json.getInt("senderTime");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
