package ro.atm.proiectretele.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import ro.atm.proiectretele.R;
import ro.atm.proiectretele.databinding.ActivityLogInBinding;
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

        FirebaseUser user = mViewModel.getUser();
        if(user != null){ // user si auth
            Log.d(TAG, "Login success");
            Intent intent = new Intent(LogInActivity.this, MediaStreamActivity.class);
            startActivity(intent);
        }else{
            Log.d(TAG, "Login error!");
        }
    }

}
