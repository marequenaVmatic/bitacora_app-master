package com.shevchenko.staffapp.connectivity;

import com.shevchenko.staffapp.connectivity.bluetooth.BTPortCommunication;
import com.shevchenko.staffapp.connectivity.protocols.Communication;

public class ReferencesStorage {
    private static ReferencesStorage instance;

    private ReferencesStorage(){

    }

    public static ReferencesStorage getInstance(){
        if (instance == null){
            instance = new ReferencesStorage();
        }

        return instance;
    }

    public BTPortCommunication btPort;
    public Communication comm;
}
