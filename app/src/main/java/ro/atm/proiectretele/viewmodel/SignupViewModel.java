package ro.atm.proiectretele.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import ro.atm.proiectretele.data.CloudFirestoreRepository;
import ro.atm.proiectretele.data.firestore_models.UserModel;
import ro.atm.proiectretele.utils.app.signup.SignupForm;

public class SignupViewModel extends AndroidViewModel {
    //// MEMBERS
    private SignupForm mSignUpForm;

    public SignupViewModel(@NonNull Application application) {
        super(application);
        mSignUpForm = new SignupForm();
    }

    //// METHODS
    public boolean onSignUp(){
        // create account
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        AtomicBoolean returnVal = new AtomicBoolean(false);
        mAuth.createUserWithEmailAndPassword(mSignUpForm.getSUeMail(), mSignUpForm.getSUpassword()).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                returnVal.set(true);
                UserModel user = new UserModel("uid", mSignUpForm.getSUeMail(), mSignUpForm.getSUusername());
                CloudFirestoreRepository.create().onUserSignUp(user);
            }
        });
        return returnVal.get();
    }

    // GETTERS AND SETTERS
    public SignupForm getSignUpForm() {
        return mSignUpForm;
    }
}
