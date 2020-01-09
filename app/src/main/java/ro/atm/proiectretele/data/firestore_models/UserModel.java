package ro.atm.proiectretele.data.firestore_models;

import ro.atm.proiectretele.utils.app.login.LogedInUser;

public class UserModel {
    //// MEMBERS
    private String mUid;
    private String mEmail;
    private String mUserName;
    private String mFirstName;
    private String mLastName;

    //// CONSTRUCTOR
    public UserModel() {
        // empty cstr is needed
    }

    public UserModel(String uid, String email, String username) {
        mUid = uid;
        mEmail = email;
        mUserName = username;
    }

    public UserModel(LogedInUser logedInUser){
        mUid = logedInUser.getUid();
        mEmail = logedInUser.getEmail();
        mUserName = logedInUser.getUserName();
        mFirstName = logedInUser.getFirstName();
        mLastName = logedInUser.gemLastName();
    }

    //// GETTERS AND SETTERS


    public String getUid() {
        return this.mUid;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String mFirstName) {
        this.mFirstName = mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String mLastName) {
        this.mLastName = mLastName;
    }
}
