package ro.atm.proiectretele.utils.app.login;

/**
 * Represents the current user that is logged in the app
 * This class is a singleton class
 *
 * @author Cristian VOICU
 */
public class LogedInUser {
    //// MEMBERS
    private static LogedInUser INSTANCE = null;

    private String mUid;
    private String mEmail;
    private String mUserName;
    private String mFirstName;
    private String mLastName;

    //// CONSTRUCTOR
    private LogedInUser(String uid, String email, String username) {
        mUid = uid;
        mEmail = email;
        mUserName = username;
    }

    private LogedInUser() {

    }

    /**
     * This method is only in LoginActivity, to set the logged in user.
     *
     * @return the class instance
     * @see ro.atm.proiectretele.view.activities.LogInActivity
     */
    public static LogedInUser getInstance(String uid, String email, String username) {
        if (INSTANCE == null) {
            INSTANCE = new LogedInUser(uid, email, username);
        }
        return INSTANCE;
    }

    /**
     * This method is used in other activities than LoginActivity to get the logged in user.
     *
     * @return null if there is no logged user or the class instance.
     */
    public static LogedInUser getInstance() {
        if (INSTANCE == null) {
            return null;
        }
        return INSTANCE;
    }
    //// METHODS


    //// GETTERS AND SETTERS
    public String getUid() {
        return mUid;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String mEmail) {
        this.mEmail = mEmail;
        //TODO: update database
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String mUserName) {
        this.mUserName = mUserName;
        //TODO: update database
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String mFirstName) {
        this.mFirstName = mFirstName;
        //TODO: update database
    }

    public String gemLastName() {
        return mLastName;
    }

    public void setLastName(String mLastName) {
        this.mLastName = mLastName;
        //TODO: update database
    }
}
