package ch.ethz.inf.vs.a4.pascalo.vs_pascalo_chatapp;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class VectorClock{
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

    public int compareToClock(VectorClock other) {

        if (myTime < other.myTime) {

            if (theirTime <= other.theirTime) {
                return -1;
            } else if (theirTime > other.theirTime){
                return 0; // Clocks are incomparable
            }

        } else if (myTime > other.myTime) {

            if (theirTime >= other.theirTime) {
                return 1;
            } else if (theirTime < other.theirTime) {
                return 0; // Clocks are incomparable
            }

        } else {

            if (theirTime < other.theirTime) {
                return -1;
            } else if (theirTime > other.theirTime) {
                return 1;
            } else {
                Log.d(VectorClock.class.getSimpleName(), "Two identical VectorClocks were compared");
                return 0; // Clocks are equal
            }
        }

        Log.d(VectorClock.class.getSimpleName(), "Catastrophic failure, this statement can't be reached");
        return 0;
    }

    // This is used to get a canonical ordering, in cases when compareToClock gives back 0 because
    // we need a way to decide on a stable total order in which to show the messages, even if the
    // clocks would have been incomparable
    public int totalOrder(VectorClock other) {

        // If both times are equal then the Clocks are really equal
        if (myTime == other.myTime && theirTime == other.theirTime) return 0;

        // Otherwise the values must be different, and we simply define the our clock
        // entry to be more important than theirs
        return myTime < other.myTime ? 1 : -1;
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

    @Override
    public boolean equals (Object obj) {
        VectorClock that = (VectorClock) obj;
        return this.myTime == that.myTime && this.theirTime == that.theirTime;
    }
}
