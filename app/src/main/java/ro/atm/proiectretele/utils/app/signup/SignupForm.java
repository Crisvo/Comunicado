package ro.atm.proiectretele.utils.app.signup;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

public class SignupForm extends BaseObservable {
    //// MEMBERS
    private String SUeMail;
    private String SUpassword;
    private String SUusername;

    //// METHODS


    //// GETTERS AND SETTERS

    @Bindable
    public String getSUeMail() {
        return SUeMail;
    }

    public void setSUeMail(String SUeMail) {
        notifyPropertyChanged(BR.sUeMail);
        this.SUeMail = SUeMail;
    }

    @Bindable
    public String getSUpassword() {
        return SUpassword;
    }

    public void setSUpassword(String SUpassword) {
        notifyPropertyChanged(BR.sUpassword);
        this.SUpassword = SUpassword;
    }

    @Bindable
    public String getSUusername() {
        return SUusername;
    }

    public void setSUusername(String SUusername) {
        notifyPropertyChanged(BR.sUusername);
        this.SUusername = SUusername;
    }
}
