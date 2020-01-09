package ro.atm.proiectretele.data;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import ro.atm.proiectretele.data.firestore_models.UserModel;

/**
 * Class that works with cloud firestore database
 * This class is singleton
 */
public class CloudFirestoreRepository {
    private static CloudFirestoreRepository INSTANCE = new CloudFirestoreRepository();
    FirebaseFirestore database = FirebaseFirestore.getInstance();
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

    public void addUser(UserModel userModel) {
        database.collection(Constants.COLLECTION_ONLINE_USERS).document(userModel.getUid()).set(userModel)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Online userModel added successfully!"))
                .addOnFailureListener(e -> Log.d(TAG, "Failed to add online userModel!"));
    }
    public void onUserSignOut(UserModel userModel){
        database.collection(Constants.COLLECTION_ONLINE_USERS).document(userModel.getUid()).delete();
    }

    public void testDB(Object object){
        database.collection(Constants.COLLECTION_COMMUNICATE).document(Constants.DOCUMENT_GENERAL_COM).set(object)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Online user added successfully!"))
                .addOnFailureListener(e -> Log.d(TAG, "Failed to add online user!"));
    }
}
