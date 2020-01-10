package ro.atm.proiectretele.utils.webrtc;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: add a description here
 * Singleton class
 *
 * @author Cristian VOICU
 * @version 1.0
 */
public class SignallingClient {
    //// MEMBERS
    private String TAG = "SignalingClient";

    //region Constructor region
    private static SignallingClient INSTANCE = new SignallingClient();
    public boolean isInitiator = false;
    public boolean isStarted = false;
    public boolean hasPandingOffer = false;
    private DocumentReference noteRef = null;
    private CollectionReference colRef = null;
    private Map<String, Object> comMap;
    private int iceNr;
    private boolean isChannelReady = false;

    private SignallingClient() {

    }

    public static SignallingClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SignallingClient();
        }
        return INSTANCE;
    }

    //endregion

    public void emitMessage(SessionDescription message) {
        Log.d("SignallingClient", "emitMessage() called with: message = [" + message + "]");
        comMap = new HashMap<>();
        comMap.put("type", message.type.canonicalForm());
        comMap.put("sdp", message.description);
        //noteRef.set(comMap);
        colRef.document("sdp").set(comMap);
    }


    public void emitIceCandidate(IceCandidate iceCandidate) {
        comMap = new HashMap<>();
        comMap.put("type", "candidate");
        comMap.put("label", iceCandidate.sdpMLineIndex);
        comMap.put("id", iceCandidate.sdpMid);
        comMap.put("candidate", iceCandidate.sdp);
        colRef.document(Integer.toString(iceNr)).set(comMap);
        iceNr ++;
        //noteRef.set(comMap);

    }

    /**
     * Sets collection where i send data.
     */
    public void setDatabaseReference(CollectionReference reference) {
        if (reference != null) {
            colRef = reference;
            isChannelReady = true;
        }
    }

    public boolean getChannelState() {
        return isChannelReady;
    }

    public int getIceNr(){
        return iceNr;
    }

    public void doRefresh() {
        iceNr = 0;
        noteRef = null;
        colRef = null;
        isStarted = false;
        isChannelReady = false;
        isInitiator = false;
        hasPandingOffer = false;
    }
}
