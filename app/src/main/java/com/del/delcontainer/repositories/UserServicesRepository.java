package com.del.delcontainer.repositories;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import com.del.delcontainer.utils.apiUtils.pojo.LinkedApplicationDetails;

import java.util.ArrayList;

public class UserServicesRepository extends BaseObservable {
    private static UserServicesRepository userServicesRepository = new UserServicesRepository();
    private ArrayList<LinkedApplicationDetails> userServicesList;

    private UserServicesRepository() {
        this.userServicesList = new ArrayList<>();
    }

    public static UserServicesRepository getInstance() {
        return userServicesRepository;
    }

    @Bindable
    public ArrayList<LinkedApplicationDetails> getUserServicesList() {
        return userServicesList;
    }

    public void setUserServicesList(ArrayList<LinkedApplicationDetails> userServicesList) {
        this.userServicesList = userServicesList;
        notifyPropertyChanged(BR.userServicesList);
    }
}
