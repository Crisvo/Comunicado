package ro.atm.proiectretele.data.firestore_models.webrtc_model;

/**
 * This class represents ice candidate model for storing to firestore database
 */
public class IceModel {
    //// MEMBERS
    /**
     * It is used to distinguish from sdp message
     */
    private String mType;
    /**
     * iceCandidate.sdpMid
     */
    private String mId;
    /**
     * iceCandidate.sdpMLineIndex
     */
    private Integer mLabel;
    /**
     * It is ice candidate sdp, candidate is the name in javascript
     */
    private String mCandidate;

    //// CONSTRUCTOR
    public IceModel() {
        // empty constructor needed
    }

    public IceModel(String type, String id, Integer label, String candidate) {
        mType = type;
        mId = id;
        mLabel = label;
        mCandidate = candidate;
    }
    //// GETTERS

    public String getType() {
        return mType;
    }

    public String getId() {
        return mId;
    }

    public Integer getLabel() {
        return mLabel;
    }

    public String getCandidate() {
        return mCandidate;
    }
}
