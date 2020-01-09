package ro.atm.proiectretele.data.firestore_models.webrtc_model;

/**
 * This class represents a sdp model for storing in fisrestore database
 */
public class SdpModel {
    //// MEMBERS
    private String mType;
    private String mSdp;

    //// CONSTRUCTOR

    public SdpModel() {
        // empty constructor needed
    }

    public SdpModel(String type, String sdp) {
        mType = type;
        mSdp = sdp;
    }

    //// GETTERS AND SETTERS

    public String getType() {
        return mType;
    }

    public String getSdp() {
        return mSdp;
    }
}
