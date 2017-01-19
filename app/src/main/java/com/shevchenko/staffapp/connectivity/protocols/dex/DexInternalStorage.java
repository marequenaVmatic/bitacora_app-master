package com.shevchenko.staffapp.connectivity.protocols.dex;

import android.os.Handler;

public class DexInternalStorage {
    private static DexInternalStorage instance;

    public static DexInternalStorage getInstance(){
        if (instance == null)
            instance = new DexInternalStorage();

        return instance;
    }


    public Handler handler;
}
