package com.shevchenko.staffapp;

/**
 * Created by shevchenko on 2015-11-26.
 *
 * This is the login part.
 * There is the online login mode and offline login mode..
 * when the online login mode is been working, the sincronzation will be appeared.
 * Sincronization has posting and loading data part..
 * Posting is uploading the data on the online server and loading is storing the data on android sqlite db.
 *
 *
 */

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.MapsInitializer;
import com.shevchenko.staffapp.Common.Common;
import com.shevchenko.staffapp.Model.GpsInfo;
import com.shevchenko.staffapp.Model.LocationLoader;
import com.shevchenko.staffapp.Model.LogEvent;
import com.shevchenko.staffapp.Model.LogFile;
import com.shevchenko.staffapp.Model.LoginUser;
import com.shevchenko.staffapp.Model.PendingTasks;
import com.shevchenko.staffapp.Model.TinTask;
import com.shevchenko.staffapp.Model.User;
import com.shevchenko.staffapp.db.DBManager;
import com.shevchenko.staffapp.net.NetworkManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LoginActivity extends Activity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_FILE = 2;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 2;
    private ProgressDialog mProgDlg;
    private EditText txtID, txtPassword;
    public static Activity loginActivity;
    private ComponentName mService;
    SharedPreferences.Editor ed;
    SharedPreferences sp;
    private String userid = "";
    private String password = "";
    LocationLoader mLocationLoader;
    private Location mNewLocation;
    GoogleApiClient mGoogleClient;
    ProgressDialog progressDialog = null;

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_FILE);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        //////////////111
        setContentView(R.layout.activity_login);

        DBManager.setContext(this);

        findViewById(R.id.btnLogin).setOnClickListener(this);
        txtID = (EditText) findViewById(R.id.txtUserID);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        loginActivity = LoginActivity.this;

            MapsInitializer.initialize(getApplicationContext());
            mLocationLoader = new LocationLoader(this, false);
            mLocationLoader
                    .SetOnLoadEventListener(new LocationLoader.OnLoadEventListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            mNewLocation = location;
                            getLocation();
                            // mUpdateLocationHandler.sendEmptyMessage(0);
                        }

                        @Override
                        public void onAddressChanged(String strAddress) {

                        }

                        @Override
                        public void onError(int iErrorCode) {
                            getLocation();

                        }
                    });
            mLocationLoader.Start();
            mGoogleClient = new GoogleApiClient.Builder(this, this, this).addApi(
                    LocationServices.API).build();

            Common.getInstance().arrIncompleteTasks.clear();
            Common.getInstance().arrCompleteTasks.clear();
            Common.getInstance().arrCategory.clear();
            Common.getInstance().arrProducto.clear();
            Common.getInstance().arrPendingTasks.clear();
            Common.getInstance().arrTaskTypes.clear();

            mProgDlg = new ProgressDialog(this);
            mProgDlg.setCancelable(false);
            mProgDlg.setMessage("Wait a little!");
            sp = getSharedPreferences("userinfo", 1);
            ed = sp.edit();

            if (sp.getBoolean("login", false)) {

                new Thread(mRunnable_offline).start();
            }
    }
    private Runnable mRunnable_offline = new Runnable() {

        @Override
        public void run() {
            long lLastClosedTime = sp.getLong(Common.PREF_KEY_CLOSEDTIME, 0);
            long lLastClosedTimeDiff = System.currentTimeMillis() - lLastClosedTime;
            Intent intent = new Intent(LoginActivity.this, LoadingActivity.class);
            boolean bNeedSync = false;
            if(lLastClosedTime > 0 && lLastClosedTimeDiff > 3 * 60 * 60 * 1000) {
            //if(lLastClosedTime > 0 && lLastClosedTimeDiff > 1) {
                bNeedSync = true;
            }
            intent.putExtra("needSync", bNeedSync);
            startActivity(intent);

            boolean repeat = true;
            while (repeat){
                try{
                    Thread.sleep(2000);
                }catch (Throwable a){

                }
                if(!Common.getInstance().latitude.equals(""))
                    break;
            }
            mHandler_offline.sendEmptyMessage(bNeedSync ? 2 : 1);

        }
    };
    private Handler mHandler_offline = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            mProgDlg.hide();
            progressDialog = new ProgressDialog(loginActivity);
            progressDialog.show();
            progressDialog.setContentView(R.layout.loading);
            progressDialog.setCancelable(false);
            if (msg.what > 0) {
                //Toast.makeText(LoginActivity.this, "LoginSuccess!", Toast.LENGTH_LONG).show();
                userid = sp.getString("userid", "");
                password = sp.getString("password", "");
                User info = DBManager.getManager().getUser(userid);
                if (info != null) {
                    LoginUser user = new LoginUser();
                    user.setUserId(userid);
                    user.setPassword(password);
                    user.setFirstName(info.firstName);
                    user.setLastName(info.lastName);
                    Common.getInstance().setLoginUser(user);
                    Toast.makeText(LoginActivity.this, "Load Success!", Toast.LENGTH_SHORT).show();

                    ed.putBoolean("login", true);
                    ed.putString("userid", userid);
                    ed.putString("password", password);
                    ed.putString("firstname", Common.getInstance().getLoginUser().getFirstName());
                    ed.putString("lastname", Common.getInstance().getLoginUser().getLastName());
                    ed.putLong(Common.PREF_KEY_CLOSEDTIME, System.currentTimeMillis());
                    ed.commit();
                    if(msg.what == 1) {
                        gotoMain();
                    } else {
                        postPendingTask();
                        //new Thread(mRunnable_Reload).start();
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "You can`t login now!!!", Toast.LENGTH_SHORT).show();
                }

            } else if (msg.what == 0) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Login failed!Please check id and password", Toast.LENGTH_LONG).show();
            } else {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Login failed due to netwrok problem", Toast.LENGTH_LONG).show();
            }

        }

    };
    private void getLocation() {
        if (mNewLocation == null) {
            double fLat = sp.getFloat(Common.PREF_KEY_LATEST_LAT, 0);
            double fLng = sp.getFloat(Common.PREF_KEY_LATEST_LNG, 0);
            if(fLat == 0) {
                settingsrequest();
                return;
            }
            Common.getInstance().latitude = String.valueOf(fLat);
            Common.getInstance().longitude = String.valueOf(fLng);
        } else {
            Common.getInstance().latitude = String.valueOf(mNewLocation.getLatitude());
            Common.getInstance().longitude = String.valueOf(mNewLocation.getLongitude());

            ed.putFloat(Common.PREF_KEY_LATEST_LAT, (float)mNewLocation.getLatitude());
            ed.putFloat(Common.PREF_KEY_LATEST_LNG, (float)mNewLocation.getLongitude());
            ed.commit();
        }
    }
    public void settingsrequest()
    {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(LoginActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
// Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        //mLocationLoader.Start();
                        mLocationLoader.Start();
                        break;
                    case Activity.RESULT_CANCELED:
                        settingsrequest();//keep asking if imp or do whatever
                        break;
                }
                break;
        }
    }
    private void DialogSelectOption() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Upload the pending tasks.")
                .setMessage("Will you upload the pending tasks?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
                        intent.putExtra("enabled", true);
                        sendBroadcast(intent);

                        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                        if(!provider.contains("gps")){ //if gps is disabled
                            final Intent poke = new Intent();
                            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                            poke.setData(Uri.parse("3"));
                            sendBroadcast(poke);


                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    /*private Runnable mRunnable_auto = new Runnable() {

        @Override
        public void run() {
            startActivity(new Intent(LoginActivity.this, LoadingActivity.class));
            userid = sp.getString("userid", "");
            password = sp.getString("password", "");
            mHandler.sendEmptyMessage(NetworkManager.getManager().login(userid, password));

        }
    };*/

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v.getId() == R.id.btnLogin) {
            if (checkField()) {
                //mProgDlg.show();
                new Thread(mRunnable).start();
            }

        }
    }

    private int postAllPendingTask() {
        ArrayList<PendingTasks> tasks = DBManager.getManager().getPendingTask(Common.getInstance().getLoginUser().getUserId());
        int sum = 0;
        for (int i = 0; i < tasks.size(); i++) {
            String[] arrPhotos = new String[]{null, null, null, null, null};
            int nCurIndex = 0;
            if (tasks.get(i).file1 != null) {
                arrPhotos[nCurIndex] = tasks.get(i).file1;
                nCurIndex++;
            }
            if (tasks.get(i).file2 != null) {
                arrPhotos[nCurIndex] = tasks.get(i).file2;
                nCurIndex++;
            }
            if (tasks.get(i).file3 != null) {
                arrPhotos[nCurIndex] = tasks.get(i).file3;
                nCurIndex++;
            }
            if (tasks.get(i).file4 != null) {
                arrPhotos[nCurIndex] = tasks.get(i).file4;
                nCurIndex++;
            }
            if (tasks.get(i).file5 != null) {
                arrPhotos[nCurIndex] = tasks.get(i).file5;
                nCurIndex++;
            }
            Boolean bRet1 = NetworkManager.getManager().postTask(tasks.get(i).taskid, tasks.get(i).date, tasks.get(i).tasktype, tasks.get(i).RutaAbastecimiento, tasks.get(i).TaskBusinessKey, tasks.get(i).Customer, tasks.get(i).Adress, tasks.get(i).LocationDesc, tasks.get(i).Model, tasks.get(i).latitude, tasks.get(i).longitude, tasks.get(i).epv, tasks.get(i).logLatitude, tasks.get(i).logLongitude, tasks.get(i).ActionDate, tasks.get(i).MachineType, tasks.get(i).Signature, tasks.get(i).NumeroGuia, tasks.get(i).Aux_valor1, tasks.get(i).Aux_valor2, tasks.get(i).Aux_valor3, tasks.get(i).Aux_valor4, tasks.get(i).Aux_valor5, tasks.get(i).Glosa, arrPhotos, nCurIndex, tasks.get(i).Completed, tasks.get(i).Comment, tasks.get(i).Aux_valor6, tasks.get(i).QuantityResumen, tasks.get(i).comment_notcap);
            if (!bRet1)
                return 0;
            DBManager.getManager().deletePendingTask(Common.getInstance().getLoginUser().getUserId(), tasks.get(i).taskid);
        }
        return 1;
    }

    private int postAllLogEvents() {
        ArrayList<LogEvent> logs = DBManager.getManager().getLogEvents(Common.getInstance().getLoginUser().getUserId());
        int sum = 0;
        for (int i = 0; i < logs.size(); i++) {

            Boolean bRet1 = NetworkManager.getManager().postLogEvent(logs.get(i));
            if (bRet1)
                DBManager.getManager().deleteLogEvent(Common.getInstance().getLoginUser().getUserId(), logs.get(i).datetime);
            else
                return 0;
        }
        return 1;
    }

    private int postAllTinPendingTask() {
        ArrayList<TinTask> tasks = DBManager.getManager().getTinPendingTask(Common.getInstance().getLoginUser().getUserId());
        int sum = 0;
        for (int i = 0; i < tasks.size(); i++) {

            Boolean bRet1 = NetworkManager.getManager().postTinTask(tasks.get(i));
            if (bRet1)
                DBManager.getManager().deletePendingTinTask(Common.getInstance().getLoginUser().getUserId(), tasks.get(i).taskid);
            else
                return 0;
        }
        return 1;
    }

    private void postPendingTask() {
        mProgDlg.show();
        new Thread(mRunnable_pendingtasks).start();

    }

    private Runnable mRunnable_Reload = new Runnable() {
        @Override
        public void run() {

            postAllPendingTask();
            postAllTinPendingTask();
            postAllLogEvents();
            postAllLogFile();

            mHandler_Reload.sendEmptyMessage(0);
        }
    };
    private Handler mHandler_Reload = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            gotoMain();
        }
    };
    private void gotoMain() {
        if(LoadingActivity.loadingActivity != null)
            LoadingActivity.loadingActivity.finish();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("position", 0);
        startActivity(intent);
        finish();
    }
    private int postAllLogFile(){
        ArrayList<LogFile> logs = DBManager.getManager().getLogFiles();
        int sum = 0;
        for (int i = 0; i < logs.size(); i++) {

            Boolean bRet1 = NetworkManager.getManager().postLogFile(logs.get(i));
            if (bRet1)
                DBManager.getManager().deleteLogFile(logs.get(i));
            else
                return 0;
        }
        return 1;
    }

    private Runnable mRunnable_pendingtasks = new Runnable() {
        @Override
        public void run() {
            int versionCode = BuildConfig.VERSION_CODE;
            String versionName = BuildConfig.VERSION_NAME;

            setService("Vendroid Inicio de sincronización, Version ."+ versionName);
            postAllPendingTask();
            postAllTinPendingTask();
            postAllLogEvents();
            mHandler_pendingtasks.sendEmptyMessage(1);

        }
    };
    private Handler mHandler_pendingtasks = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            mProgDlg.hide();
            if (msg.what == 1) {
                Toast.makeText(LoginActivity.this, "Pending Tasks was uploaded!!!", Toast.LENGTH_SHORT).show();
                loadTasks();
            } else if (msg.what == 0) {
                Toast.makeText(LoginActivity.this, "Pending Tasks was failed to upload!!!", Toast.LENGTH_SHORT).show();
                loadTasks();
            }
        }
    };

    @Override
    public void onBackPressed() {
        //LoginActivity.this.finish();
        moveTaskToBack(true);
        System.exit(0);
    }

    private void loadTasks() {

        mProgDlg.show();
        new Thread(mRunnable_tasks).start();
    }

    private Runnable mRunnable_tasks = new Runnable() {

        @Override
        public void run() {

            int nRet = NetworkManager.getManager().loadTasks(Common.getInstance().arrIncompleteTasks, Common.getInstance().arrCompleteTasks, Common.getInstance().arrCompleteTinTasks, Common.getInstance().arrCompleteDetailCounters, Common.getInstance().arrCommentErrors);
            NetworkManager.getManager().loadCategory(Common.getInstance().arrCategory, Common.getInstance().arrProducto, Common.getInstance().arrProducto_Ruta, Common.getInstance().arrUsers, Common.getInstance().arrTaskTypes);
            NetworkManager.getManager().loadMachine(Common.getInstance().arrMachineCounters);
            DBManager.getManager().deleteAllIncompleteTask(Common.getInstance().getLoginUser().getUserId());
            DBManager.getManager().deleteAllCompleteTask(Common.getInstance().getLoginUser().getUserId());
            DBManager.getManager().deleteAllCompleteTinTask(Common.getInstance().getLoginUser().getUserId());
            DBManager.getManager().deleteAllProducto();
            DBManager.getManager().deleteAllProducto_Ruta();
            DBManager.getManager().deleteAllCategory();
            DBManager.getManager().deleteAllUser();
            DBManager.getManager().deleteAllTypes();
            DBManager.getManager().deleteAllMachineCounter();
            DBManager.getManager().deleteAllCompleteDetailCounter();
            DBManager.getManager().deleteAllErrors();

            for (int i = 0; i < Common.getInstance().arrIncompleteTasks.size(); i++) {
                DBManager.getManager().insertInCompleteTask(Common.getInstance().arrIncompleteTasks.get(i));
            }
            for (int i = 0; i < Common.getInstance().arrCompleteTasks.size(); i++) {
                DBManager.getManager().insertCompleteTask(Common.getInstance().arrCompleteTasks.get(i));
            }
            for (int i = 0; i < Common.getInstance().arrCompleteTinTasks.size(); i++) {
                DBManager.getManager().insertCompleteTinTask(Common.getInstance().arrCompleteTinTasks.get(i));
            }
            for (int i = 0; i < Common.getInstance().arrCompleteDetailCounters.size(); i++) {
                DBManager.getManager().insertCompleteDetailCounter(Common.getInstance().arrCompleteDetailCounters.get(i));
            }
            for (int i = 0; i < Common.getInstance().arrCategory.size(); i++) {
                DBManager.getManager().insertCategory(Common.getInstance().arrCategory.get(i));
            }
            for (int i = 0; i < Common.getInstance().arrProducto.size(); i++) {
                DBManager.getManager().insertProducto(Common.getInstance().arrProducto.get(i));
            }
            for (int i = 0; i < Common.getInstance().arrProducto_Ruta.size(); i++) {
                DBManager.getManager().insertProducto_Ruta(Common.getInstance().arrProducto_Ruta.get(i));
            }
            for (int i = 0; i < Common.getInstance().arrUsers.size(); i++) {
                DBManager.getManager().insertUser(Common.getInstance().arrUsers.get(i));
            }
            for(int i = 0; i < Common.getInstance().arrTaskTypes.size(); i++){
                DBManager.getManager().insertType(Common.getInstance().arrTaskTypes.get(i));
            }
            for(int i = 0; i < Common.getInstance().arrMachineCounters.size(); i++){
                DBManager.getManager().insertMachineCounter(Common.getInstance().arrMachineCounters.get(i));
            }
            for(int i = 0;  i < Common.getInstance().arrCommentErrors.size(); i++){
                DBManager.getManager().insertError(Common.getInstance().arrCommentErrors.get(i));
            }
            //NetworkManager.getManager().loadProducto(Common.getInstance().arrProducto);
            Common.getInstance().arrPendingTasks = DBManager.getManager().getPendingTask(Common.getInstance().getLoginUser().getUserId());
            Common.getInstance().arrTinTasks = DBManager.getManager().getTinPendingTask(Common.getInstance().getLoginUser().getUserId());
            mHandler_task.sendEmptyMessage(nRet);
        }
    };
    private Handler mHandler_task = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            mProgDlg.hide();
            if (msg.what == 0) {
                Toast.makeText(LoginActivity.this, "Load Success!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("position", 0);
                startActivity(intent);
            } else if (msg.what == 1) {
                Toast.makeText(LoginActivity.this, "Load failed!", Toast.LENGTH_SHORT).show();
            } else if (msg.what == -1) {
                Toast.makeText(LoginActivity.this, "Load failed due to network problem! Please check your network status", Toast.LENGTH_SHORT).show();
            }
            //setTaskNumber();
        }
    };
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            mProgDlg.hide();
            if(Common.getInstance().getLoginUser() == null) {
                LoadingActivity loading = (LoadingActivity) LoadingActivity.loadingActivity;
                if(loading != null) loading.finish();
                Toast.makeText(LoginActivity.this, "Login failed! Please check id and password", Toast.LENGTH_LONG).show();
            } else {
                startActivity(new Intent(LoginActivity.this, LoadingActivity.class));
                ed.putBoolean("login", true);
                ed.putString("userid", userid);
                ed.putString("password", password);
                ed.putString("firstname", Common.getInstance().getLoginUser().getFirstName());
                ed.putString("lastname", Common.getInstance().getLoginUser().getLastName());
                ed.putLong(Common.PREF_KEY_CLOSEDTIME, System.currentTimeMillis());
                ed.commit();
                postPendingTask();
            }
            /*if (msg.what > 0) {
                //Toast.makeText(LoginActivity.this, "LoginSuccess!", Toast.LENGTH_LONG).show();
                ed.putBoolean("login", true);
                ed.putString("userid", userid);
                ed.putString("password", password);
                ed.commit();
                //Common.getInstance().setUserID(userid);

                postPendingTask();
                //loadTasks();

            } else if (msg.what == 0) {
                LoadingActivity loading = (LoadingActivity) LoadingActivity.loadingActivity;
                if(loading != null) loading.finish();
                Toast.makeText(LoginActivity.this, "Login failed! Please check id and password", Toast.LENGTH_LONG).show();
            } else {
                LoadingActivity loading = (LoadingActivity) LoadingActivity.loadingActivity;
                if(loading != null) loading.finish();
                Toast.makeText(LoginActivity.this, "Login failed due to netwrok problem", Toast.LENGTH_LONG).show();
            }*/
        }

    };
    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            userid = txtID.getText().toString();
            password = txtPassword.getText().toString();
            Common.getInstance().setLoginUser(NetworkManager.getManager().login(userid, password));
            mHandler.sendEmptyMessage(0);

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean checkField() {
        EditText txtId = (EditText) findViewById(R.id.txtUserID);
        EditText txtPwd = (EditText) findViewById(R.id.txtPassword);
        if (txtId.getText().toString().isEmpty() || txtPwd.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.insert_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean getConnectivityStatus() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return true;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return true;
        }
        return false;
    }
    @Override
    protected void onStart() {
        super.onStart();

        mGoogleClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleClient.disconnect();

        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {

    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, 0);
            } catch (IntentSender.SendIntentException e) {
            }
        } else {
        }
    }


    public void setService(String description) {

        GpsInfo info = new GpsInfo(this);
        Intent service = new Intent(this, LogService.class);
        service.putExtra("userid", Common.getInstance().getLoginUser().getUserId());
        service.putExtra("taskid", "Activity Login");
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        service.putExtra("datetime", time);
        service.putExtra("description", description);
        service.putExtra("latitude", Common.getInstance().latitude);
        service.putExtra("longitude", Common.getInstance().longitude);
        mService = startService(service);
    }
}
