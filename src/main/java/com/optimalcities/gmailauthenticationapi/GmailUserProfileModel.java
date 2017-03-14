package com.optimalcities.gmailauthenticationapi;

import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

/**
 * Created by obelix on 12/03/2017.
 */

//Data Binding Class
public class GmailUserProfileModel extends BaseObservable {

    public final ObservableBoolean isConnected = new ObservableBoolean(true);

    public final ObservableField<String> userName = new ObservableField<String>();
    public final ObservableField<String> userConnected = new ObservableField<String>();
    public final ObservableField<String> userEmail = new ObservableField<String>();
    public final ObservableField<String> userFullName = new ObservableField<String>();
    public final ObservableField<String> userGivenName = new ObservableField<String>();
    public final ObservableField<String> userFamilyName = new ObservableField<String>();


    public final ObservableField<String> imageUrl = new ObservableField<String>();




    public GmailUserProfileModel(){

    }
    public GmailUserProfileModel(String userEmail,
                                 boolean connected,
                                 String imageLink,
                                 String familyName,
                                 String givenName,
                                 String fullName) {
        this.userEmail.set(userEmail);
        this.isConnected.set(connected);
        this.userConnected.set("User Connected");
        this.imageUrl.set(imageLink);
        this.userFamilyName.set(familyName);
        this.userGivenName.set(givenName);
        this.userFullName.set(fullName);
    }

    public void changeLogin(String userName){

        this.userName.set(userName);

    }

    public void changeStatus(boolean status) {
        isConnected.set(status);

        if(isConnected.get()){
            this.userConnected.set("User Connected");

        }
        else{
            this.userConnected.set("User Disconnected");

        }
    }

}
