package ro.atm.proiectretele.data.firestore_models;

public class MessageModel {
    private String mFrom;
    private String mMessage;
    private String mTimestamp;
    private String mTo;

    public MessageModel(String from, String message) {
        this.mFrom = from;
        this.mMessage = message;
    }

    public MessageModel(String from, String message, String to) {
        this.mFrom = from;
        this.mMessage = message;
        mTo = to;
    }

    public MessageModel() {

    }

    public String getFrom() {
        return mFrom;
    }

    public void setFrom(String mFrom) {
        this.mFrom = mFrom;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String mMessage) {
        this.mMessage = mMessage;
    }

    public String getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(String mTimestamp) {
        this.mTimestamp = mTimestamp;
    }

    public String getTo() {
        return mTo;
    }

    public void setTo(String mTo) {
        this.mTo = mTo;
    }
}

