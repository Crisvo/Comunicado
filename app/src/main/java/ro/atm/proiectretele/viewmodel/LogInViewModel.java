package ro.atm.proiectretele.viewmodel;

import android.app.Application;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;

import androidx.lifecycle.AndroidViewModel;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.reactivex.Completable;
import ro.atm.proiectretele.utils.app.login.LoginForm;

public class LogInViewModel extends AndroidViewModel {
    //// MEMBERS
    private LoginForm mLoginForm;
    private FirebaseUser mUser = null;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public LogInViewModel(@NonNull Application application) {
        super(application);
        mLoginForm = new LoginForm();

    }

    //// METHODS
    public void onEmailLogin(View view) {
        mAuth.signInWithEmailAndPassword(mLoginForm.getEmail(), mLoginForm.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    //Log.d(TAG, "signInWithEmail:success");
                    mUser = mAuth.getCurrentUser();
                } else {
                    // If sign in fails, display a message to the user.
                    //Log.w(TAG, "signInWithEmail:failure", task.getException());
                }

            }
        });
    }

    //// GETTERS AND SETTERS
    public LoginForm getLoginForm() {
        return mLoginForm;
    }

    public FirebaseUser getUser() {
        return mUser;
    }
}
