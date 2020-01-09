package ro.atm.proiectretele.utils.webrtc;

import android.util.Log;

import com.firebase.ui.firestore.paging.FirestoreDataSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: add a description here
 * Singleton class
 * @author Cristian VOICU
 * @version 1.0
 */
public class SignallingClient {

    private static SignallingClient INSTANCE = new SignallingClient();
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private DocumentReference noteRef = database.document("user-com/sig-client");
    private Map<String, Object> comMap;

    //// MEMBERS
    private String TAG = "SignalingClient";

    private SignallingClient(){

    }
    //// CONSTRUCTOR
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
        comMap = new HashMap<>();
        comMap.put("create or join", message);
        noteRef.set(comMap);
    }

    public void emitMessage(String message) {
        Log.d("SignallingClient", "emitMessage() called with: message = [" + message + "]");
        //socket.emit("message", message);
        comMap = new HashMap<>();
        comMap.put("message", message);
        noteRef.set(comMap);
    }

    public void emitMessage(SessionDescription message) {
            Log.d("SignallingClient", "emitMessage() called with: message = [" + message + "]");
            comMap = new HashMap<>();
            comMap.put("type", message.type.canonicalForm());
            comMap.put("sdp", message.description);
            //socket.emit("message", obj);
            noteRef.set(comMap);
    }


    public void emitIceCandidate(IceCandidate iceCandidate) {
            //socket.emit("message", object);
            comMap = new HashMap<>();
            comMap.put("type", "candidate");
            comMap.put("label", iceCandidate.sdpMLineIndex);
            comMap.put("id", iceCandidate.sdpMid);
            comMap.put("candidate", iceCandidate.sdp);
            noteRef.set(comMap);

    }
}
