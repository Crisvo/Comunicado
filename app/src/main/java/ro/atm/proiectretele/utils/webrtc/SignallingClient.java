package ro.atm.proiectretele.utils.webrtc;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

/**
 * TODO: add a description here
 * Singleton class
 * @author Cristian VOICU
 * @version 1.0
 */
public class SignallingClient {
    //// CONSTRUCTOR
    private static SignallingClient INSTANCE = new SignallingClient();
    //// MEMBERS
    private String TAG = "SignalingClient";

    private SignallingClient(){

    }

    public static SignallingClient getInstance(){
        if(INSTANCE == null){
            INSTANCE = new SignallingClient();
        }
        return INSTANCE;
    }

    //// METHODS
    private void emitInitStatement(String message) {
        Log.d("SignallingClient", "emitInitStatement() called with: event = [" + "create or join" + "], message = [" + message + "]");
       // socket.emit("create or join", message);
    }

    public void emitMessage(String message) {
        Log.d("SignallingClient", "emitMessage() called with: message = [" + message + "]");
        //socket.emit("message", message);
    }

    public void emitMessage(SessionDescription message) {
        try {
            Log.d("SignallingClient", "emitMessage() called with: message = [" + message + "]");
            JSONObject obj = new JSONObject();
            obj.put("type", message.type.canonicalForm());
            obj.put("sdp", message.description);
            Log.d("emitMessage", obj.toString());
            Log.d("vivek1794", obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void emitIceCandidate(IceCandidate iceCandidate) {
        try {
            JSONObject object = new JSONObject();
            object.put("type", "candidate");
            object.put("label", iceCandidate.sdpMLineIndex);
            object.put("id", iceCandidate.sdpMid);
            object.put("candidate", iceCandidate.sdp);
            //socket.emit("message", object);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
