package ro.atm.proiectretele.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.view.View;

import ro.atm.proiectretele.R;
import ro.atm.proiectretele.databinding.ActivitySignupBinding;
import ro.atm.proiectretele.viewmodel.SignupViewModel;

public class SignupActivity extends AppCompatActivity {
    //// MEMBERS
    private ActivitySignupBinding binding;
    private SignupViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup);
        mViewModel = ViewModelProviders.of(this).get(SignupViewModel.class);

        binding.setViewModel(mViewModel);
    }

    //// METHODS

    public void onSignUp(View view){
        if(mViewModel.onSignUp()){
            finish();
            this.onBackPressed();
        }else{
            // Show error.
        }
    }
}
