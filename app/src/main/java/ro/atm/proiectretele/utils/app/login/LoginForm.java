package ro.atm.proiectretele.utils.app.login;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

/***/
public class LoginForm extends BaseObservable {
    //// MEMBERS
    private String mEmail = "";
    private String mPassword = "";

    //// CONSTRUCTOR

    //// METHODS

    //// GETTERS AND SETTERS

    @Bindable
    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String mEmail) {
        notifyPropertyChanged(BR.email);
        this.mEmail = mEmail;
    }

    @Bindable
    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String mPassword) {
        notifyPropertyChanged(BR.password);
        this.mPassword = mPassword;
    }

}
