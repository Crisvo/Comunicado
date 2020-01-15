package ro.atm.proiectretele.data;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import ro.atm.proiectretele.data.firestore_models.MessageModel;
import ro.atm.proiectretele.data.firestore_models.UserModel;
import ro.atm.proiectretele.utils.app.login.LogedInUser;

/**
 * Class that works with cloud firestore database
 * This class is singleton
 */
public class CloudFirestoreRepository {
    private static CloudFirestoreRepository INSTANCE = new CloudFirestoreRepository();
    FirebaseFirestore database = FirebaseFirestore.getInstance();

    CollectionReference usersReference = database.collection(Constants.COLLECTION_ALL_USERS);
    CollectionReference userCommunicationReference = database.collection(Constants.COLLECTION_COMMUNICATE);
    //// MEMBERS
    private String TAG = "FirebaseDatabase";

    //// CONSTRUCTOR
    private CloudFirestoreRepository() {

    }

    public static CloudFirestoreRepository create() {
        if (INSTANCE == null) {
            INSTANCE = new CloudFirestoreRepository();
        }
        return INSTANCE;
    }

    //// METHODS

    public void addOnlineUser(UserModel userModel) {
        database.collection(Constants.COLLECTION_ONLINE_USERS).document(userModel.getUid()).set(userModel)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Online userModel added successfully!"))
                .addOnFailureListener(e -> Log.d(TAG, "Failed to add online userModel!"));
    }

    public void onUserSignOut(UserModel userModel) {
        database.collection(Constants.COLLECTION_ONLINE_USERS).document(userModel.getUid()).delete();
    }

    public void onUserSignUp(UserModel user) {
        usersReference.document(user.getEmail()).set(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User added successfully! on signup"))
                .addOnFailureListener(e -> Log.d(TAG, "Failed to add user! on signup"));
    }

    public void onUserLogedIn() {
        LogedInUser user = LogedInUser.getInstance();
        if (user != null) {
            userCommunicationReference.document(user.getEmail()).set(new UserModel(user));
        }
    }

    public void onMessageSend(Map messageModel, String to){
        database.collection(to).add(messageModel);

    }
}
