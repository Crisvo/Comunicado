package ro.atm.proiectretele.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ro.atm.proiectretele.R;
import ro.atm.proiectretele.data.CloudFirestoreRepository;
import ro.atm.proiectretele.data.firestore_models.UserModel;
import ro.atm.proiectretele.databinding.ActivityLogInBinding;
import ro.atm.proiectretele.utils.app.login.LogedInUser;
import ro.atm.proiectretele.viewmodel.LogInViewModel;

public class LogInActivity extends AppCompatActivity {
    private String TAG = "LoginActivity";
    //// MEMBERS
    private ActivityLogInBinding binding;
    private LogInViewModel mViewModel;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_log_in);
        mViewModel = ViewModelProviders.of(this).get(LogInViewModel.class);
        binding.setViewModel(mViewModel);
        mAuth.setLanguageCode("ro");

    }

    //// METHODS
    public void onSignUp(View view){
        Intent intent = new Intent(LogInActivity.this, SignupActivity.class);
        startActivity(intent);
    }

    public void onLogin(View view){
        mViewModel.onEmailLogin(view);

        //TODO: this is temporary
        LogedInUser logedInUser = LogedInUser.getInstance("test", "test", "test");
        Intent intent = new Intent(LogInActivity.this, MainActivity.class);
        startActivity(intent);
/*
        FirebaseUser user = mViewModel.getUser();
        if(user != null){ // user si auth
            LogedInUser logedInUser = LogedInUser.getInstance(user.getUid(), user.getEmail(), user.getDisplayName());
            CloudFirestoreRepository repo = CloudFirestoreRepository.create();
            repo.addUser(new UserModel(logedInUser));
            repo.onUserLogedIn();
            Log.d(TAG, "Login success");
            Intent intent = new Intent(LogInActivity.this, MainActivity.class);
            startActivity(intent);
        }else{
            Log.d(TAG, "Login error!");
        }*/
    }

}
